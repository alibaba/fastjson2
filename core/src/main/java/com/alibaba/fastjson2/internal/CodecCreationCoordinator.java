package com.alibaba.fastjson2.internal;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Coordinates synchronous reader and writer creation on concurrent cache misses.
 * Lock entries are reference-counted so holders and queued callers always use the
 * same lock, and the entry can be removed without retaining the associated type.
 * A shared per-thread wait context detects dependency cycles across both providers.
 */
public final class CodecCreationCoordinator {
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private CodecCreationCoordinator() {
    }

    public static final class LockEntry {
        final ReentrantLock lock = new ReentrantLock();
        final AtomicInteger references = new AtomicInteger();
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
        private boolean closed;

        private Scope(
                ConcurrentMap<Type, LockEntry> locks,
                Type type,
                LockEntry createLock,
                boolean outermost,
                boolean locked
        ) {
            this.locks = locks;
            this.type = type;
            this.createLock = createLock;
            this.outermost = outermost;
            this.locked = locked;
        }

        public boolean isCycleDetected() {
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
                createLock.lock.unlock();
            }
            release(locks, type, createLock);
        }
    }

    public static Scope acquire(ConcurrentMap<Type, LockEntry> locks, Type type) {
        LockEntry createLock = locks.compute(type, (key, lock) -> {
            if (lock == null) {
                lock = new LockEntry();
            }
            lock.references.incrementAndGet();
            return lock;
        });

        Context context = CONTEXT.get();
        boolean outermost = context == null;
        boolean locked = false;
        try {
            if (outermost) {
                createLock.lock.lock();
                locked = true;
                context = new Context();
                CONTEXT.set(context);
            } else if (createLock.lock.tryLock()) {
                locked = true;
            } else {
                context.waitingFor = createLock;
                if (hasDependencyCycle(createLock, context)) {
                    context.waitingFor = null;
                    return new Scope(locks, type, createLock, false, false);
                }
                try {
                    createLock.lock.lock();
                    locked = true;
                } finally {
                    context.waitingFor = null;
                }
            }

            if (createLock.lock.getHoldCount() == 1) {
                createLock.owner = context;
            }
            return new Scope(locks, type, createLock, outermost, true);
        } catch (Throwable error) {
            if (context != null) {
                context.waitingFor = null;
            }
            if (outermost) {
                CONTEXT.remove();
            }
            if (locked) {
                if (createLock.lock.getHoldCount() == 1) {
                    createLock.owner = null;
                }
                createLock.lock.unlock();
            }
            release(locks, type, createLock);
            throw error;
        }
    }

    private static void release(ConcurrentMap<Type, LockEntry> locks, Type type, LockEntry createLock) {
        if (createLock.references.decrementAndGet() == 0) {
            locks.computeIfPresent(type, (key, current) ->
                    current == createLock && createLock.references.get() == 0 ? null : current
            );
        }
    }

    private static boolean hasDependencyCycle(LockEntry createLock, Context current) {
        IdentityHashMap<Context, Boolean> checked = new IdentityHashMap<>();
        Context owner = createLock.owner;
        while (owner != null && checked.put(owner, Boolean.TRUE) == null) {
            if (owner == current) {
                return true;
            }
            LockEntry waitingFor = owner.waitingFor;
            owner = waitingFor == null ? null : waitingFor.owner;
        }
        return false;
    }
}
