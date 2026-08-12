package com.alibaba.fastjson2.reader;

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

public class ObjectReaderProviderConcurrentTest {
    @Test
    public void testCreateObjectReaderOnce() throws InterruptedException {
        assertCreateObjectReaderOnce(false);
    }

    @Test
    public void testCreateFieldBasedObjectReaderOnce() throws InterruptedException {
        ObjectReaderTestResult result = assertCreateObjectReaderOnce(true);
        ObjectReader fieldBasedReader = result.readers.iterator().next();
        ObjectReader methodBasedReader = result.provider.getObjectReader(Bean.class, false);

        assertNotSame(fieldBasedReader, methodBasedReader);
        assertEquals(2, result.createCount.get());
    }

    @Test
    public void testNestedCreationDoesNotDeadlock() throws InterruptedException {
        CountDownLatch creating = new CountDownLatch(2);
        ThreadLocal<Boolean> nested = new ThreadLocal<>();
        ObjectReaderCreator creator = new ObjectReaderCreator() {
            @Override
            public <T> ObjectReader<T> createObjectReader(
                    Class<T> objectClass,
                    Type objectType,
                    boolean fieldBased,
                    ObjectReaderProvider provider
            ) {
                if ((objectClass == BeanA.class || objectClass == BeanB.class) && nested.get() == null) {
                    nested.set(Boolean.TRUE);
                    try {
                        creating.countDown();
                        if (!creating.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("object readers were not created concurrently");
                        }
                        provider.getObjectReader(objectClass == BeanA.class ? BeanB.class : BeanA.class);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    } finally {
                        nested.remove();
                    }
                }
                return super.createObjectReader(objectClass, objectType, fieldBased, provider);
            }
        };
        ObjectReaderProvider provider = new ObjectReaderProvider(creator);
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(2);

        startDaemonThread(() -> getObjectReader(provider, BeanA.class, error, done));
        startDaemonThread(() -> getObjectReader(provider, BeanB.class, error, done));

        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
    }

    @Test
    public void testParameterizedTypesCreateConcurrently() throws InterruptedException {
        CountDownLatch creating = new CountDownLatch(2);
        AtomicInteger createCount = new AtomicInteger();
        ObjectReaderCreator creator = new ObjectReaderCreator() {
            @Override
            public <T> ObjectReader<T> createObjectReader(
                    Class<T> objectClass,
                    Type objectType,
                    boolean fieldBased,
                    ObjectReaderProvider provider
            ) {
                if (objectClass == GenericBean.class) {
                    createCount.incrementAndGet();
                    creating.countDown();
                    try {
                        if (!creating.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("parameterized readers were serialized");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
                return super.createObjectReader(objectClass, objectType, fieldBased, provider);
            }
        };
        ObjectReaderProvider provider = new ObjectReaderProvider(creator);
        Type typeA = new TypeReference<GenericBean<ValueA>>() { }.getType();
        Type typeB = new TypeReference<GenericBean<ValueB>>() { }.getType();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(2);

        startDaemonThread(() -> getObjectReader(provider, typeA, error, done));
        startDaemonThread(() -> getObjectReader(provider, typeB, error, done));

        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(2, createCount.get());
    }

    private ObjectReaderTestResult assertCreateObjectReaderOnce(boolean fieldBased) throws InterruptedException {
        int threadCount = 32;
        AtomicInteger createCount = new AtomicInteger();
        ObjectReaderCreator creator = new ObjectReaderCreator() {
            @Override
            public <T> ObjectReader<T> createObjectReader(
                    Class<T> objectClass,
                    Type objectType,
                    boolean fieldBased,
                    ObjectReaderProvider provider
            ) {
                createCount.incrementAndGet();
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return super.createObjectReader(objectClass, objectType, fieldBased, provider);
            }
        };
        ObjectReaderProvider provider = new ObjectReaderProvider(creator);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        Set<ObjectReader> readers = ConcurrentHashMap.newKeySet();
        AtomicReference<Throwable> error = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            startDaemonThread(() -> {
                ready.countDown();
                try {
                    start.await();
                    readers.add(provider.getObjectReader(Bean.class, fieldBased));
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
        assertEquals(1, readers.size());
        assertEquals(1, createCount.get());
        return new ObjectReaderTestResult(provider, readers, createCount);
    }

    private static void getObjectReader(
            ObjectReaderProvider provider,
            Type type,
            AtomicReference<Throwable> error,
            CountDownLatch done
    ) {
        try {
            provider.getObjectReader(type);
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

    static final class ObjectReaderTestResult {
        final ObjectReaderProvider provider;
        final Set<ObjectReader> readers;
        final AtomicInteger createCount;

        ObjectReaderTestResult(
                ObjectReaderProvider provider,
                Set<ObjectReader> readers,
                AtomicInteger createCount
        ) {
            this.provider = provider;
            this.readers = readers;
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
