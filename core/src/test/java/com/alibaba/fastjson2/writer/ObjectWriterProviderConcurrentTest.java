package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
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
    }

    @Test
    public void testCreateModuleObjectWriterOnce() throws InterruptedException {
        AtomicInteger moduleCount = new AtomicInteger();
        ObjectWriterProvider provider = new ObjectWriterProvider();
        provider.register(new ObjectWriterModule() {
            @Override
            public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
                if (objectType != ModuleBean.class) {
                    return null;
                }

                moduleCount.incrementAndGet();
                sleep(100);
                return ObjectWriterImplString.INSTANCE;
            }
        });

        Set<ObjectWriter> writers = getWriters(provider, ModuleBean.class, false, 32);
        assertEquals(1, writers.size());
        assertEquals(1, moduleCount.get());
    }

    @Test
    public void testCreatorFailureDoesNotReplaceLiveLock() throws InterruptedException {
        AtomicInteger createCount = new AtomicInteger();
        AtomicInteger concurrentCount = new AtomicInteger();
        AtomicInteger maxConcurrentCount = new AtomicInteger();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch failFirst = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch finishSecond = new CountDownLatch(1);
        ObjectWriterCreator creator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                int concurrent = concurrentCount.incrementAndGet();
                maxConcurrentCount.accumulateAndGet(concurrent, Math::max);
                try {
                    int count = createCount.incrementAndGet();
                    if (count == 1) {
                        firstStarted.countDown();
                        await(failFirst);
                        throw new IllegalStateException("first creation failed");
                    }
                    if (count == 2) {
                        secondStarted.countDown();
                        await(finishSecond);
                    }
                    return super.createObjectWriter(objectClass, features, provider);
                } finally {
                    concurrentCount.decrementAndGet();
                }
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        CountDownLatch done = new CountDownLatch(3);
        AtomicReference<Throwable> error = new AtomicReference<>();

        startDaemonThread(() -> getObjectWriter(provider, FailureBean.class, new AtomicReference<>(), done));
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
        startDaemonThread(() -> getObjectWriter(provider, FailureBean.class, error, done));
        sleep(100);
        failFirst.countDown();
        assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
        sleep(100);
        startDaemonThread(() -> getObjectWriter(provider, FailureBean.class, error, done));
        sleep(200);

        assertEquals(2, createCount.get());
        assertEquals(1, maxConcurrentCount.get());
        finishSecond.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(2, createCount.get());
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
                sleep(1200);
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        Set<ObjectWriter> writers = getWriters(provider, Bean.class, fieldBased, 32);

        assertEquals(1, writers.size());
        assertEquals(1, createCount.get());
        return new ObjectWriterTestResult(provider, writers, createCount);
    }

    private static Set<ObjectWriter> getWriters(
            ObjectWriterProvider provider,
            Type type,
            boolean fieldBased,
            int threadCount
    ) throws InterruptedException {
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
                    writers.add(provider.getObjectWriter(type, (Class) type, fieldBased));
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
        return writers;
    }

    private static void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
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

    public static class ModuleBean {
    }

    public static class FailureBean {
    }

    public static class GenericBean<T> {
        public T value;
    }

    public static class ValueA {
    }

    public static class ValueB {
    }
}
