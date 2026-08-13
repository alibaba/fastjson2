package com.alibaba.fastjson2.internal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        while (thread.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
            TimeUnit.MILLISECONDS.sleep(1);
        }
        assertEquals(Thread.State.WAITING, thread.getState());
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
}
