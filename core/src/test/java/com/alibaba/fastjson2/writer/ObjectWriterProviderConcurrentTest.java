package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectWriterProviderConcurrentTest {
    @Test
    public void testCreateObjectWriterOnce() throws InterruptedException {
        assertCreateObjectWriterOnce(false);
    }

    @Test
    public void testCreateFieldBasedObjectWriterOnce() throws InterruptedException {
        ObjectWriterTestResult result = assertCreateObjectWriterOnce(true);
        ObjectWriter fieldBasedWriter = result.writers.iterator().next();
        ObjectWriter methodBasedWriter = result.provider.getObjectWriter(Bean.class, Bean.class, false);

        assertNotSame(fieldBasedWriter, methodBasedWriter);
        assertEquals(2, result.createCount.get());
    }

    @Test
    public void testNestedCreationDoesNotDeadlock() throws InterruptedException {
        CountDownLatch creating = new CountDownLatch(2);
        ThreadLocal<Boolean> nested = new ThreadLocal<>();
        ObjectWriterCreator creator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                if ((objectClass == BeanA.class || objectClass == BeanB.class) && nested.get() == null) {
                    nested.set(Boolean.TRUE);
                    try {
                        creating.countDown();
                        if (!creating.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("object writers were not created concurrently");
                        }
                        provider.getObjectWriter(objectClass == BeanA.class ? BeanB.class : BeanA.class);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    } finally {
                        nested.remove();
                    }
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(2);

        startDaemonThread(() -> getObjectWriter(provider, BeanA.class, error, done));
        startDaemonThread(() -> getObjectWriter(provider, BeanB.class, error, done));

        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
    }

    @Test
    public void testParameterizedTypesCreateConcurrently() throws InterruptedException {
        CountDownLatch creating = new CountDownLatch(2);
        AtomicInteger createCount = new AtomicInteger();
        ObjectWriterCreator creator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                if (objectClass == GenericBean.class) {
                    createCount.incrementAndGet();
                    creating.countDown();
                    try {
                        if (!creating.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("parameterized writers were serialized");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        Type typeA = new TypeReference<GenericBean<ValueA>>() { }.getType();
        Type typeB = new TypeReference<GenericBean<ValueB>>() { }.getType();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(2);

        startDaemonThread(() -> getObjectWriter(provider, typeA, error, done));
        startDaemonThread(() -> getObjectWriter(provider, typeB, error, done));

        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(2, createCount.get());
    }

    private ObjectWriterTestResult assertCreateObjectWriterOnce(boolean fieldBased) throws InterruptedException {
        int threadCount = 32;
        AtomicInteger createCount = new AtomicInteger();
        ObjectWriterCreator creator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                createCount.incrementAndGet();
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        Set<ObjectWriter> writers = ConcurrentHashMap.newKeySet();
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            startDaemonThread(() -> {
                ready.countDown();
                try {
                    start.await();
                    writers.add(provider.getObjectWriter(Bean.class, Bean.class, fieldBased));
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    done.countDown();
                }
            });
        }

        boolean allReady = ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        assertTrue(allReady);
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(1, writers.size());
        assertEquals(1, createCount.get());
        return new ObjectWriterTestResult(provider, writers, createCount);
    }

    private static void getObjectWriter(
            ObjectWriterProvider provider,
            Type type,
            AtomicReference<Throwable> error,
            CountDownLatch done
    ) {
        try {
            provider.getObjectWriter(type);
        } catch (Throwable e) {
            error.compareAndSet(null, e);
        } finally {
            done.countDown();
        }
    }

    private static void startDaemonThread(Runnable task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    static final class ObjectWriterTestResult {
        final ObjectWriterProvider provider;
        final Set<ObjectWriter> writers;
        final AtomicInteger createCount;

        ObjectWriterTestResult(
                ObjectWriterProvider provider,
                Set<ObjectWriter> writers,
                AtomicInteger createCount
        ) {
            this.provider = provider;
            this.writers = writers;
            this.createCount = createCount;
        }
    }

    public static class Bean {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class BeanA {
    }

    public static class BeanB {
    }

    public static class GenericBean<T> {
        public T value;
    }

    public static class ValueA {
    }

    public static class ValueB {
    }
}
