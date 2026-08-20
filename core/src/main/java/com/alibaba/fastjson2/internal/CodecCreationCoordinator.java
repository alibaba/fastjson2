package com.alibaba.fastjson2.internal;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Coordinates synchronous reader and writer creation on concurrent cache misses.
 * Lock entries are reference-counted so holders and queued callers always use the
 * same lock, and the entry can be removed without retaining the associated type.
 * A shared per-thread wait context detects dependency cycles across both providers.
 * Waits that cross external synchronization, such as class initialization, are
 * bounded; lock-free fallbacks are serialized and rate-limited per entry to avoid
 * a creation burst.
 */
public final class CodecCreationCoordinator {
    private static final long WAIT_NANOS = TimeUnit.SECONDS.toNanos(5);
    private static final long FALLBACK_INTERVAL_NANOS = WAIT_NANOS;
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private CodecCreationCoordinator() {
    }

    public static final class LockEntry {
        final ReentrantLock lock = new ReentrantLock();
        final AtomicInteger references = new AtomicInteger();
        final AtomicReference<Context> fallbackOwner = new AtomicReference<>();
        final AtomicLong nextFallbackNanos = new AtomicLong();
        volatile Context owner;
        volatile Throwable failure;

        private LockEntry() {
        }
    }

    private static final class Context {
        volatile LockEntry waitingFor;
    }

    public static final class Scope implements AutoCloseable {
        private final ConcurrentMap<Type, LockEntry> locks;
        private final Type type;
        private final LockEntry createLock;
        private final boolean outermost;
        private final boolean locked;
        private final boolean cycleDetected;
        private final Context fallbackOwner;
        private boolean closed;

        private Scope(
                ConcurrentMap<Type, LockEntry> locks,
                Type type,
                LockEntry createLock,
                boolean outermost,
                boolean locked,
                boolean cycleDetected,
                Context fallbackOwner
        ) {
            this.locks = locks;
            this.type = type;
            this.createLock = createLock;
            this.outermost = outermost;
            this.locked = locked;
            this.cycleDetected = cycleDetected;
            this.fallbackOwner = fallbackOwner;
        }

        public boolean isCycleDetected() {
            return cycleDetected;
        }

        public boolean isLockFreeFallback() {
            return !locked;
        }

        public void throwIfFailed() {
            Throwable failure = createLock.failure;
            if (failure instanceof RuntimeException) {
                throw (RuntimeException) failure;
            }
            if (failure instanceof Error) {
                throw (Error) failure;
            }
        }

        public void fail(Throwable failure) {
            if (locked) {
                createLock.failure = failure;
            }
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;

            if (outermost) {
                CONTEXT.remove();
            }
            if (locked) {
                if (createLock.lock.getHoldCount() == 1) {
                    createLock.owner = null;
                }
                try {
                    release(locks, type, createLock);
                } finally {
                    createLock.lock.unlock();
                }
            } else {
                try {
                    release(locks, type, createLock);
                } finally {
                    if (fallbackOwner != null) {
                        createLock.fallbackOwner.compareAndSet(fallbackOwner, null);
                    }
                }
            }
        }
    }

    public static Scope acquire(ConcurrentMap<Type, LockEntry> locks, Type type) {
        return acquire(locks, type, WAIT_NANOS, FALLBACK_INTERVAL_NANOS);
    }

    static Scope acquire(
            ConcurrentMap<Type, LockEntry> locks,
            Type type,
            long waitNanos,
            long fallbackIntervalNanos
    ) {
        LockEntry createLock = locks.compute(type, (key, lock) -> {
            if (lock == null) {
                lock = new LockEntry();
            }
            lock.references.incrementAndGet();
            return lock;
        });

        Context context = CONTEXT.get();
        boolean outermost = context == null;
        if (outermost) {
            context = new Context();
            CONTEXT.set(context);
        }
        boolean locked = false;
        Context fallbackOwner = null;
        try {
            if (!outermost
                    && (createLock.owner == context || createLock.fallbackOwner.get() == context)) {
                return new Scope(locks, type, createLock, false, false, true, null);
            }
            if (createLock.lock.tryLock()) {
                locked = true;
            } else {
                context.waitingFor = createLock;
                if (!outermost && hasDependencyCycle(createLock, context)) {
                    context.waitingFor = null;
                    return new Scope(locks, type, createLock, false, false, true, null);
                }
                try {
                    locked = lockOrReserveFallback(
                            createLock,
                            waitNanos,
                            fallbackIntervalNanos,
                            context
                    );
                } finally {
                    context.waitingFor = null;
                }
                if (!locked) {
                    fallbackOwner = context;
                    return new Scope(locks, type, createLock, outermost, false, false, fallbackOwner);
                }
            }

            if (createLock.lock.getHoldCount() == 1) {
                createLock.owner = context;
            }
            return new Scope(locks, type, createLock, outermost, true, false, null);
        } catch (Throwable error) {
            if (context != null) {
                context.waitingFor = null;
            }
            if (outermost) {
                CONTEXT.remove();
            }
            if (fallbackOwner != null) {
                createLock.fallbackOwner.compareAndSet(fallbackOwner, null);
            }
            if (locked) {
                if (createLock.lock.getHoldCount() == 1) {
                    createLock.owner = null;
                }
                try {
                    release(locks, type, createLock);
                } finally {
                    createLock.lock.unlock();
                }
            } else {
                release(locks, type, createLock);
            }
            throw error;
        }
    }

    public static <T> T publish(ConcurrentMap<Type, T> cache, Type type, T value) {
        T previous = cache.putIfAbsent(type, value);
        return previous != null ? previous : value;
    }

    private static void release(ConcurrentMap<Type, LockEntry> locks, Type type, LockEntry createLock) {
        locks.compute(type, (key, current) -> {
            boolean alive = createLock.references.decrementAndGet() > 0;
            return current == createLock && !alive ? null : current;
        });
    }

    private static boolean lockOrReserveFallback(
            LockEntry createLock,
            long waitNanos,
            long fallbackIntervalNanos,
            Context context
    ) {
        boolean interrupted = false;
        try {
            for (;;) {
                long deadline = System.nanoTime() + waitNanos;
                for (;;) {
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0) {
                        break;
                    }
                    try {
                        if (createLock.lock.tryLock(remaining, TimeUnit.NANOSECONDS)) {
                            return true;
                        }
                        break;
                    } catch (InterruptedException ignored) {
                        interrupted = true;
                    }
                }

                if (createLock.lock.tryLock()) {
                    return true;
                }

                long now = System.nanoTime();
                long nextFallback = createLock.nextFallbackNanos.get();
                if ((nextFallback == 0 || now - nextFallback >= 0)
                        && createLock.fallbackOwner.compareAndSet(null, context)) {
                    createLock.nextFallbackNanos.set(now + fallbackIntervalNanos);
                    return false;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static boolean hasDependencyCycle(LockEntry createLock, Context current) {
        IdentityHashMap<Context, Boolean> checked = new IdentityHashMap<>();
        ArrayDeque<Context> pending = new ArrayDeque<>();
        addOwners(createLock, pending);
        while (!pending.isEmpty()) {
            Context owner = pending.removeLast();
            if (owner == current) {
                return true;
            }
            if (checked.put(owner, Boolean.TRUE) != null) {
                continue;
            }
            LockEntry waitingFor = owner.waitingFor;
            if (waitingFor != null) {
                addOwners(waitingFor, pending);
            }
        }
        return false;
    }

    private static void addOwners(LockEntry createLock, ArrayDeque<Context> pending) {
        Context owner = createLock.owner;
        if (owner != null) {
            pending.addLast(owner);
        }
        Context fallbackOwner = createLock.fallbackOwner.get();
        if (fallbackOwner != null && fallbackOwner != owner) {
            pending.addLast(fallbackOwner);
        }
    }
}
