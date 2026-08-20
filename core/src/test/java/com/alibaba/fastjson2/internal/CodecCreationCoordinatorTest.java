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
    public void testDependencyCycleAcrossLockMapsIsDetected() throws InterruptedException {
        long waitNanos = TimeUnit.SECONDS.toNanos(2);
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> readerLocks = new ConcurrentHashMap<>();
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> writerLocks = new ConcurrentHashMap<>();
        CountDownLatch outerLocksAcquired = new CountDownLatch(2);
        CountDownLatch done = new CountDownLatch(2);
        AtomicInteger cycles = new AtomicInteger();
        AtomicInteger timedFallbacks = new AtomicInteger();
        AtomicReference<Throwable> error = new AtomicReference<>();

        startCrossMapCycleThread(
                readerLocks,
                BeanA.class,
                writerLocks,
                BeanB.class,
                waitNanos,
                outerLocksAcquired,
                done,
                cycles,
                timedFallbacks,
                error
        );
        startCrossMapCycleThread(
                writerLocks,
                BeanB.class,
                readerLocks,
                BeanA.class,
                waitNanos,
                outerLocksAcquired,
                done,
                cycles,
                timedFallbacks,
                error
        );

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertTrue(cycles.get() >= 1);
        assertEquals(0, timedFallbacks.get());
        assertTrue(readerLocks.isEmpty());
        assertTrue(writerLocks.isEmpty());
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
    public void testSameThreadDependencyCycleDoesNotReenterLock() {
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();

        try (CodecCreationCoordinator.Scope outer =
                     CodecCreationCoordinator.acquire(locks, Bean.class)) {
            assertFalse(outer.isCycleDetected());
            CodecCreationCoordinator.LockEntry lockEntry = locks.get(Bean.class);
            assertEquals(1, lockEntry.lock.getHoldCount());

            try (CodecCreationCoordinator.Scope nested =
                         CodecCreationCoordinator.acquire(locks, Bean.class)) {
                assertTrue(nested.isCycleDetected());
                assertTrue(nested.isLockFreeFallback());
                assertEquals(1, lockEntry.lock.getHoldCount());
            }
        }

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
    public void testExternalWaitUsesSingleActiveFallback() throws InterruptedException {
        int threadCount = 8;
        long waitNanos = TimeUnit.MILLISECONDS.toNanos(50);
        long fallbackIntervalNanos = TimeUnit.MILLISECONDS.toNanos(100);
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
        CountDownLatch continueFallback = new CountDownLatch(1);
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
                            assertTrue(continueFallback.await(5, TimeUnit.SECONDS));
                        }
                    }
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    done.countDown();
                }
            });
        }

        try {
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (locks.get(Bean.class).references.get() < threadCount + 1
                    && System.nanoTime() < deadline) {
                TimeUnit.MILLISECONDS.sleep(1);
            }
            assertEquals(threadCount + 1, locks.get(Bean.class).references.get());
            assertTrue(fallbackEntered.await(5, TimeUnit.SECONDS));
            TimeUnit.MILLISECONDS.sleep(350);
            assertEquals(1, fallbacks.get());
        } finally {
            start.countDown();
            first.close();
            continueFallback.countDown();
        }

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(1, fallbacks.get());
        assertTrue(locks.isEmpty());
    }

    @Test
    public void testFallbackReservationIsReleasedWithScope() throws InterruptedException {
        long waitNanos = TimeUnit.MILLISECONDS.toNanos(10);
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();
        CodecCreationCoordinator.Scope first = CodecCreationCoordinator.acquire(
                locks,
                Bean.class,
                waitNanos,
                0
        );
        AtomicInteger fallbacks = new AtomicInteger();
        AtomicReference<Throwable> error = new AtomicReference<>();

        try {
            for (int i = 0; i < 2; i++) {
                CountDownLatch done = new CountDownLatch(1);
                startDaemonThread(() -> {
                    try (CodecCreationCoordinator.Scope scope = CodecCreationCoordinator.acquire(
                            locks,
                            Bean.class,
                            waitNanos,
                            0
                    )) {
                        assertTrue(scope.isLockFreeFallback());
                        fallbacks.incrementAndGet();
                    } catch (Throwable e) {
                        error.compareAndSet(null, e);
                    } finally {
                        done.countDown();
                    }
                });
                assertTrue(done.await(5, TimeUnit.SECONDS));
                assertNull(error.get());
            }
        } finally {
            first.close();
        }

        assertEquals(2, fallbacks.get());
        assertTrue(locks.isEmpty());
    }

    @Test
    public void testFallbackOwnerSameThreadReentryIsCycle() throws InterruptedException {
        long waitNanos = TimeUnit.MILLISECONDS.toNanos(10);
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();
        CodecCreationCoordinator.Scope first = CodecCreationCoordinator.acquire(
                locks,
                Bean.class,
                waitNanos,
                0
        );
        CountDownLatch fallbackEntered = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<Boolean> cycleDetected = new AtomicReference<>();

        startDaemonThread(() -> {
            try (CodecCreationCoordinator.Scope fallback = CodecCreationCoordinator.acquire(
                    locks,
                    Bean.class,
                    waitNanos,
                    0
            )) {
                assertTrue(fallback.isLockFreeFallback());
                assertFalse(fallback.isCycleDetected());
                fallbackEntered.countDown();
                try (CodecCreationCoordinator.Scope nested = CodecCreationCoordinator.acquire(
                        locks,
                        Bean.class,
                        waitNanos,
                        0
                )) {
                    cycleDetected.set(nested.isCycleDetected());
                }
            } catch (Throwable e) {
                error.compareAndSet(null, e);
            } finally {
                done.countDown();
            }
        });

        boolean fallbackSeen;
        boolean completedWhileFirstHeld;
        try {
            fallbackSeen = fallbackEntered.await(5, TimeUnit.SECONDS);
            completedWhileFirstHeld = fallbackSeen
                    && done.await(2, TimeUnit.SECONDS);
        } finally {
            first.close();
        }

        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertTrue(fallbackSeen);
        assertTrue(completedWhileFirstHeld);
        assertNull(error.get());
        assertEquals(Boolean.TRUE, cycleDetected.get());
        assertTrue(locks.isEmpty());
    }

    @Test
    public void testDependencyCycleThroughFallbackOwnerIsDetected() throws InterruptedException {
        long waitNanos = TimeUnit.MILLISECONDS.toNanos(10);
        ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks = new ConcurrentHashMap<>();
        CodecCreationCoordinator.Scope first = CodecCreationCoordinator.acquire(
                locks,
                BeanA.class,
                waitNanos,
                0
        );
        CountDownLatch secondLockAcquired = new CountDownLatch(1);
        CountDownLatch checkCycle = new CountDownLatch(1);
        CountDownLatch fallbackEntered = new CountDownLatch(1);
        CountDownLatch fallbackDone = new CountDownLatch(1);
        CountDownLatch secondDone = new CountDownLatch(1);
        AtomicReference<Boolean> cycleDetected = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        startDaemonThread(() -> {
            try (CodecCreationCoordinator.Scope second =
                         CodecCreationCoordinator.acquire(locks, BeanB.class)) {
                secondLockAcquired.countDown();
                await(checkCycle);
                try (CodecCreationCoordinator.Scope nested = CodecCreationCoordinator.acquire(
                        locks,
                        BeanA.class,
                        waitNanos,
                        0
                )) {
                    cycleDetected.set(nested.isCycleDetected());
                }
            } catch (Throwable e) {
                error.compareAndSet(null, e);
            } finally {
                secondDone.countDown();
            }
        });

        Thread fallbackThread = startDaemonThread(() -> {
            try {
                assertTrue(secondLockAcquired.await(5, TimeUnit.SECONDS));
                try (CodecCreationCoordinator.Scope fallback = CodecCreationCoordinator.acquire(
                        locks,
                        BeanA.class,
                        waitNanos,
                        0
                )) {
                    assertTrue(fallback.isLockFreeFallback());
                    assertFalse(fallback.isCycleDetected());
                    fallbackEntered.countDown();
                    try (CodecCreationCoordinator.Scope ignored =
                                 CodecCreationCoordinator.acquire(locks, BeanB.class)) {
                        // The second lock owner must detect the cycle through this fallback owner.
                    }
                }
            } catch (Throwable e) {
                error.compareAndSet(null, e);
            } finally {
                fallbackDone.countDown();
            }
        });

        boolean completedWhileFirstHeld;
        try {
            assertTrue(fallbackEntered.await(5, TimeUnit.SECONDS));
            awaitWaiting(fallbackThread);
            checkCycle.countDown();
            completedWhileFirstHeld = secondDone.await(2, TimeUnit.SECONDS);
        } finally {
            checkCycle.countDown();
            first.close();
        }

        assertTrue(secondDone.await(5, TimeUnit.SECONDS));
        assertTrue(fallbackDone.await(5, TimeUnit.SECONDS));
        assertTrue(completedWhileFirstHeld);
        assertNull(error.get());
        assertEquals(Boolean.TRUE, cycleDetected.get());
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

    private static void startCrossMapCycleThread(
            ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> firstLocks,
            Type first,
            ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> secondLocks,
            Type second,
            long waitNanos,
            CountDownLatch outerLocksAcquired,
            CountDownLatch done,
            AtomicInteger cycles,
            AtomicInteger timedFallbacks,
            AtomicReference<Throwable> error
    ) {
        startDaemonThread(() -> {
            try (CodecCreationCoordinator.Scope outer = CodecCreationCoordinator.acquire(firstLocks, first)) {
                assertFalse(outer.isCycleDetected());
                outerLocksAcquired.countDown();
                if (!outerLocksAcquired.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("outer creation locks were not acquired concurrently");
                }
                try (CodecCreationCoordinator.Scope inner = CodecCreationCoordinator.acquire(
                        secondLocks,
                        second,
                        waitNanos,
                        0
                )) {
                    if (inner.isCycleDetected()) {
                        cycles.incrementAndGet();
                    } else if (inner.isLockFreeFallback()) {
                        timedFallbacks.incrementAndGet();
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
