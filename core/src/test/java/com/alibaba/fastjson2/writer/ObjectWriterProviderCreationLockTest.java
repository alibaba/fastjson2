package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Writer creation runs user code: the creator instantiates the bean for default values and builds enum field
 * writers, which initializes classes. If that ran under a provider-wide lock, a class initializer serializing on
 * another thread would wait for the lock while the lock holder waits for the initializer. Each test uses its own
 * provider and its own classes, so a regression hangs only the two threads of that test.
 */
public class ObjectWriterProviderCreationLockTest {
    static final ObjectWriterProvider CTOR_PROVIDER = new ObjectWriterProvider();
    static final CountDownLatch CTOR_A_STARTED = new CountDownLatch(1);
    static final CountDownLatch CTOR_B_IN_CREATOR = new CountDownLatch(1);
    static volatile Thread ctorA;

    static final ObjectWriterProvider ENUM_PROVIDER = new ObjectWriterProvider();
    static final CountDownLatch ENUM_A_STARTED = new CountDownLatch(1);
    static final CountDownLatch ENUM_B_SERIALIZING = new CountDownLatch(1);

    static void await(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Waits until the given thread has blocked on a monitor or finished, at most 5 s. */
    static void awaitBlockedOrDone(Thread thread) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline && thread.isAlive() && thread.getState() != Thread.State.BLOCKED) {
            Thread.yield();
        }
    }

    public static class Meta {
        public String name = "meta";
    }

    public static class Registry {
        static final String DESCRIPTION;

        static {
            CTOR_A_STARTED.countDown();
            await(CTOR_B_IN_CREATOR);
            DESCRIPTION = JSON.toJSONString(new Meta(), new JSONWriter.Context(CTOR_PROVIDER));
        }
    }

    public static class Order {
        static final AtomicInteger CONSTRUCTED = new AtomicInteger();
        public int id = 1;
        public String registry;

        public Order() {
            // the second instance is the one the creator builds for default values
            if (CONSTRUCTED.incrementAndGet() == 2) {
                CTOR_B_IN_CREATOR.countDown();
                awaitBlockedOrDone(ctorA);
                registry = Registry.DESCRIPTION;
            }
        }
    }

    @Test
    public void constructorRunByTheCreatorMayWaitForAnInitializerThatSerializes() throws Exception {
        Thread a = new Thread(() -> {
            Object description = Registry.DESCRIPTION;
        }, "class-initializer");
        a.setDaemon(true);
        ctorA = a;
        a.start();
        await(CTOR_A_STARTED);
        Thread b = new Thread(() -> JSON.toJSONString(new Order(), new JSONWriter.Context(CTOR_PROVIDER)), "first-write");
        b.setDaemon(true);
        b.start();
        a.join(10_000);
        b.join(10_000);
        assertFalse(a.isAlive() || b.isAlive(), "writer creation and a serializing class initializer deadlocked");
    }

    public static class Meta2 {
        public String name = "meta2";
    }

    public enum Color {
        RED;
        static final String DESCRIPTION;

        static {
            ENUM_A_STARTED.countDown();
            await(ENUM_B_SERIALIZING);
            // give the other thread time to enter the creator for Paint, which initializes this enum
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            DESCRIPTION = JSON.toJSONString(new Meta2(), new JSONWriter.Context(ENUM_PROVIDER));
        }
    }

    public static class Paint {
        public int id = 1;
        public Color color;
    }

    @Test
    public void enumFieldWriterMayWaitForAnEnumInitializerThatSerializes() throws Exception {
        Thread a = new Thread(() -> {
            Object description = Color.DESCRIPTION;
        }, "enum-initializer");
        a.setDaemon(true);
        a.start();
        await(ENUM_A_STARTED);
        Thread b = new Thread(() -> {
            Paint paint = new Paint();
            ENUM_B_SERIALIZING.countDown();
            JSON.toJSONString(paint, new JSONWriter.Context(ENUM_PROVIDER));
        }, "first-write");
        b.setDaemon(true);
        b.start();
        a.join(10_000);
        b.join(10_000);
        assertFalse(a.isAlive() || b.isAlive(), "writer creation and a serializing enum initializer deadlocked");
    }
}
