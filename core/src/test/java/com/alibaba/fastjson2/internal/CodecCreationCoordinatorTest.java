package com.alibaba.fastjson2.internal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class CodecCreationCoordinatorTest {
    @Test
    public void testSameTypeIsSerializedAndLockIsReleased() throws InterruptedException {
        int threadCount = 64;
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger active = new AtomicInteger();
        AtomicInteger maxActive = new AtomicInteger();
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            startDaemonThread(() -> {
                ready.countDown();
                try {
                    start.await();
                    try (CodecCreationCoordinator.Scope scope =
                                 CodecCreationCoordinator.acquire(locks, Bean.class)) {
                        assertFalse(scope.isCycleDetected());
                        int count = active.incrementAndGet();
                        maxActive.accumulateAndGet(count, Math::max);
                        TimeUnit.MILLISECONDS.sleep(2);
                        active.decrementAndGet();
                    }
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(1, maxActive.get());
        assertTrue(locks.isEmpty());
    }

    @Test
    public void testThreeThreadDependencyCycleTerminates() throws InterruptedException {
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();
        CountDownLatch outerLocksAcquired = new CountDownLatch(3);
        CountDownLatch done = new CountDownLatch(3);
        AtomicInteger cycles = new AtomicInteger();
        AtomicReference<Throwable> error = new AtomicReference<>();

        startCycleThread(locks, BeanA.class, BeanB.class, outerLocksAcquired, done, cycles, error);
        startCycleThread(locks, BeanB.class, BeanC.class, outerLocksAcquired, done, cycles, error);
        startCycleThread(locks, BeanC.class, BeanA.class, outerLocksAcquired, done, cycles, error);

        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
        assertTrue(cycles.get() >= 1);
        assertTrue(locks.isEmpty());
    }

    @Test
    public void testScopeCloseIsIdempotent() {
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();
        CodecCreationCoordinator.Scope scope = CodecCreationCoordinator.acquire(locks, Bean.class);

        scope.close();
        scope.close();

        assertTrue(locks.isEmpty());
    }

    @Test
    public void testPreInterruptedWaiterReleasesLockEntry() throws InterruptedException {
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();
        CodecCreationCoordinator.Scope first = CodecCreationCoordinator.acquire(locks, Bean.class);
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<Boolean> interrupted = new AtomicReference<>();

        Thread waiter = startDaemonThread(() -> {
            Thread.currentThread().interrupt();
            try (CodecCreationCoordinator.Scope ignored =
                         CodecCreationCoordinator.acquire(locks, Bean.class)) {
                interrupted.set(Thread.currentThread().isInterrupted());
            } catch (Throwable e) {
                error.set(e);
            } finally {
                done.countDown();
            }
        });

        awaitWaiting(waiter);
        first.close();

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(Boolean.TRUE, interrupted.get());
        assertTrue(locks.isEmpty());
    }

    @Test
    public void testFailureDoesNotLeakAcrossLockGenerations() throws InterruptedException {
        ReleaseRaceMap locks = new ReleaseRaceMap();
        IllegalStateException expectedError = new IllegalStateException("first creation failed");
        CountDownLatch firstAcquired = new CountDownLatch(1);
        CountDownLatch startRelease = new CountDownLatch(1);
        CountDownLatch firstDone = new CountDownLatch(1);
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        Thread first = startDaemonThread(() -> {
            locks.releaseThread = Thread.currentThread();
            try {
                CodecCreationCoordinator.Scope scope = CodecCreationCoordinator.acquire(locks, Bean.class);
                scope.fail(expectedError);
                locks.coordinateRelease = true;
                firstAcquired.countDown();
                await(startRelease);
                scope.close();
            } catch (Throwable error) {
                firstError.set(error);
            } finally {
                firstDone.countDown();
            }
        });

        assertTrue(firstAcquired.await(5, TimeUnit.SECONDS));
        startRelease.countDown();
        assertTrue(locks.releaseLinearized.await(5, TimeUnit.SECONDS));

        CountDownLatch retryDone = new CountDownLatch(1);
        AtomicReference<Throwable> retryError = new AtomicReference<>();
        startDaemonThread(() -> {
            try (CodecCreationCoordinator.Scope scope =
                         CodecCreationCoordinator.acquire(locks, Bean.class)) {
                scope.throwIfFailed();
            } catch (Throwable error) {
                retryError.set(error);
            } finally {
                retryDone.countDown();
            }
        });

        assertTrue(locks.freshComputeStarted.await(5, TimeUnit.SECONDS));
        locks.continueRelease.countDown();
        assertTrue(firstDone.await(5, TimeUnit.SECONDS));
        assertTrue(retryDone.await(5, TimeUnit.SECONDS));
        first.join(1000);

        assertNull(firstError.get());
        assertNull(retryError.get());
        assertTrue(locks.isEmpty());
    }

    @Test
    public void testExternalWaitUsesSingleRateLimitedFallback() throws InterruptedException {
        int threadCount = 8;
        long waitNanos = TimeUnit.MILLISECONDS.toNanos(100);
        long fallbackIntervalNanos = TimeUnit.SECONDS.toNanos(1);
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();
        CodecCreationCoordinator.Scope first = CodecCreationCoordinator.acquire(
                locks,
                Bean.class,
                waitNanos,
                fallbackIntervalNanos
        );
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch fallbackEntered = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger fallbacks = new AtomicInteger();
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            startDaemonThread(() -> {
                ready.countDown();
                try {
                    start.await();
                    try (CodecCreationCoordinator.Scope scope = CodecCreationCoordinator.acquire(
                            locks,
                            Bean.class,
                            waitNanos,
                            fallbackIntervalNanos
                    )) {
                        if (scope.isLockFreeFallback()) {
                            assertFalse(scope.isCycleDetected());
                            fallbacks.incrementAndGet();
                            fallbackEntered.countDown();
                        }
                    }
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(ready.await(5, TimeUnit.SECONDS));
        start.countDown();
        assertTrue(fallbackEntered.await(5, TimeUnit.SECONDS));
        first.close();

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(1, fallbacks.get());
        assertTrue(locks.isEmpty());
    }

    private static void startCycleThread(
            ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks,
            Type first,
            Type second,
            CountDownLatch outerLocksAcquired,
            CountDownLatch done,
            AtomicInteger cycles,
            AtomicReference<Throwable> error
    ) {
        startDaemonThread(() -> {
            try (CodecCreationCoordinator.Scope outer = CodecCreationCoordinator.acquire(locks, first)) {
                assertFalse(outer.isCycleDetected());
                outerLocksAcquired.countDown();
                if (!outerLocksAcquired.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("outer creation locks were not acquired concurrently");
                }
                try (CodecCreationCoordinator.Scope inner = CodecCreationCoordinator.acquire(locks, second)) {
                    if (inner.isCycleDetected()) {
                        cycles.incrementAndGet();
                    }
                }
            } catch (Throwable e) {
                error.compareAndSet(null, e);
            } finally {
                done.countDown();
            }
        });
    }

    private static void awaitWaiting(Thread thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!isWaiting(thread) && System.nanoTime() < deadline) {
            TimeUnit.MILLISECONDS.sleep(1);
        }
        assertTrue(isWaiting(thread));
    }

    private static boolean isWaiting(Thread thread) {
        Thread.State state = thread.getState();
        return state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING;
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(error);
        }
    }

    private static Thread startDaemonThread(Runnable task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public static class Bean {
    }

    public static class BeanA {
    }

    public static class BeanB {
    }

    public static class BeanC {
    }

    static final class ReleaseRaceMap
            extends ConcurrentHashMap<Type, CodecCreationCoordinator.LockEntry> {
        final CountDownLatch releaseLinearized = new CountDownLatch(1);
        final CountDownLatch freshComputeStarted = new CountDownLatch(1);
        final CountDownLatch freshComputed = new CountDownLatch(1);
        final CountDownLatch continueRelease = new CountDownLatch(1);
        volatile Thread releaseThread;
        volatile boolean coordinateRelease;

        @Override
        public CodecCreationCoordinator.LockEntry compute(
                Type key,
                BiFunction<? super Type, ? super CodecCreationCoordinator.LockEntry,
                        ? extends CodecCreationCoordinator.LockEntry> remappingFunction
        ) {
            if (coordinateRelease && Thread.currentThread() == releaseThread) {
                return super.compute(key, (type, current) -> {
                    CodecCreationCoordinator.LockEntry result = remappingFunction.apply(type, current);
                    releaseLinearized.countDown();
                    await(continueRelease);
                    return result;
                });
            }

            if (coordinateRelease) {
                freshComputeStarted.countDown();
            }
            CodecCreationCoordinator.LockEntry result = super.compute(key, remappingFunction);
            if (coordinateRelease) {
                freshComputed.countDown();
            }
            return result;
        }

        @Override
        public CodecCreationCoordinator.LockEntry computeIfPresent(
                Type key,
                BiFunction<? super Type, ? super CodecCreationCoordinator.LockEntry,
                        ? extends CodecCreationCoordinator.LockEntry> remappingFunction
        ) {
            if (coordinateRelease && Thread.currentThread() == releaseThread) {
                releaseLinearized.countDown();
                await(freshComputed);
            }
            return super.computeIfPresent(key, remappingFunction);
        }
    }
}
