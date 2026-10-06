package com.alibaba.fastjson2.internal;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Coordinates synchronous reader and writer creation on concurrent cache misses.
 * Lock entries are reference-counted so holders and queued callers always use the
 * same lock, and the entry can be removed without retaining the associated type.
 * A shared per-thread wait context detects dependency cycles across both providers.
 * Timed fallback admissions are rate-limited per entry, but an older fallback
 * must not prevent progress through external synchronization, such as class
 * initialization. Waiters can reuse a published cache value without acquiring
 * either the creation lock or a fallback admission.
 */
public final class CodecCreationCoordinator {
    private static final long WAIT_NANOS = TimeUnit.SECONDS.toNanos(5);
    private static final long FALLBACK_INTERVAL_NANOS = WAIT_NANOS;
    private static final long CACHE_CHECK_NANOS = TimeUnit.MILLISECONDS.toNanos(50);
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private CodecCreationCoordinator() {
    }

    static final class FailureRecord {
        final Throwable error;

        FailureRecord(Throwable error) {
            this.error = error;
        }
    }

    public static final class LockEntry {
        final ReentrantLock lock = new ReentrantLock();
        final AtomicInteger references = new AtomicInteger();
        final ConcurrentMap<Context, Scope> fallbackOwners = new ConcurrentHashMap<>();
        final AtomicLong nextFallbackNanos = new AtomicLong();
        volatile Context owner;
        volatile FailureRecord failure;

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
        private final Context context;
        private final boolean outermost;
        private final FailureRecord observedFailure;
        // Populated during acquire, before this thread-confined scope is returned.
        private boolean locked;
        private boolean cycleDetected;
        private Object cachedValue;
        private boolean closed;

        private Scope(
                ConcurrentMap<Type, LockEntry> locks,
                Type type,
                LockEntry createLock,
                Context context,
                boolean outermost,
                FailureRecord observedFailure
        ) {
            this.locks = locks;
            this.type = type;
            this.createLock = createLock;
            this.context = context;
            this.outermost = outermost;
            this.observedFailure = observedFailure;
        }

        public boolean isCycleDetected() {
            return cycleDetected;
        }

        public boolean isLockFreeFallback() {
            return !locked && cachedValue == null;
        }

        public Object getCachedValue() {
            return cachedValue;
        }

        private void registerFallback() {
            createLock.fallbackOwners.putIfAbsent(context, this);
        }

        public void throwIfFailed() {
            FailureRecord current = createLock.failure;
            if (current == null || current == observedFailure) {
                return;
            }

            Throwable error = current.error;
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            if (error instanceof Error) {
                throw (Error) error;
            }
        }

        public void fail(Throwable failure) {
            if (locked) {
                createLock.failure = new FailureRecord(failure);
            }
        }

        /**
         * Records a successful cache observation or publication. A success
         * supersedes any earlier creation failure retained by this entry.
         */
        public <T> T complete(T value) {
            createLock.failure = null;
            return value;
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
                    // A reentrant scope must not remove its enclosing scope's registration.
                    createLock.fallbackOwners.remove(context, this);
                }
            }
        }
    }

    public static Scope acquire(ConcurrentMap<Type, LockEntry> locks, Type type) {
        return acquire(locks, type, null);
    }

    public static Scope acquire(ConcurrentMap<Type, LockEntry> locks, Type type, ConcurrentMap<Type, ?> cache) {
        return acquire(locks, type, cache, WAIT_NANOS, FALLBACK_INTERVAL_NANOS);
    }

    static Scope acquire(
            ConcurrentMap<Type, LockEntry> locks,
            Type type,
            long waitNanos,
            long fallbackIntervalNanos
    ) {
        return acquire(locks, type, null, waitNanos, fallbackIntervalNanos);
    }

    static Scope acquire(
            ConcurrentMap<Type, LockEntry> locks,
            Type type,
            ConcurrentMap<Type, ?> cache,
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
        FailureRecord observedFailure = createLock.failure;

        Context context = CONTEXT.get();
        boolean outermost = context == null;
        if (outermost) {
            context = new Context();
            CONTEXT.set(context);
        }
        Scope scope = new Scope(locks, type, createLock, context, outermost, observedFailure);
        try {
            if (!outermost
                    && (createLock.owner == context || createLock.fallbackOwners.containsKey(context))) {
                scope.cycleDetected = true;
                return scope;
            }
            scope.locked = createLock.lock.tryLock();
            if (!scope.locked) {
                context.waitingFor = createLock;
                try {
                    awaitCreation(scope, cache, waitNanos, fallbackIntervalNanos);
                } finally {
                    context.waitingFor = null;
                }
            }

            if (scope.locked && createLock.lock.getHoldCount() == 1) {
                createLock.owner = context;
            }
            return scope;
        } catch (Throwable error) {
            scope.close();
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

    private static void awaitCreation(
            Scope scope,
            ConcurrentMap<Type, ?> cache,
            long waitNanos,
            long fallbackIntervalNanos
    ) {
        LockEntry createLock = scope.createLock;
        long deadline = System.nanoTime() + waitNanos;
        boolean interrupted = false;
        try {
            for (;;) {
                if (cache != null) {
                    scope.cachedValue = cache.get(scope.type);
                    if (scope.cachedValue != null) {
                        return;
                    }
                }
                scope.locked = createLock.lock.tryLock();
                if (scope.locked) {
                    return;
                }
                if (!scope.outermost && hasDependencyCycle(createLock, scope.context)) {
                    scope.cycleDetected = true;
                    scope.registerFallback();
                    return;
                }

                long now = System.nanoTime();
                long remaining = deadline - now;
                if (remaining <= 0) {
                    long nextFallback = createLock.nextFallbackNanos.get();
                    if (tryReserveFallback(createLock, nextFallback, now, fallbackIntervalNanos)) {
                        scope.registerFallback();
                        return;
                    }
                    remaining = CACHE_CHECK_NANOS;
                }
                try {
                    scope.locked = createLock.lock.tryLock(
                            Math.min(remaining, CACHE_CHECK_NANOS), TimeUnit.NANOSECONDS);
                    if (scope.locked) {
                        return;
                    }
                } catch (InterruptedException ignored) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static boolean tryReserveFallback(LockEntry createLock, long previous, long now, long interval) {
        return (previous == 0 || now - previous >= 0)
                && createLock.nextFallbackNanos.compareAndSet(previous, now + interval);
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
        for (Context fallbackOwner : createLock.fallbackOwners.keySet()) {
            if (fallbackOwner != owner) {
                pending.addLast(fallbackOwner);
            }
        }
    }
}
