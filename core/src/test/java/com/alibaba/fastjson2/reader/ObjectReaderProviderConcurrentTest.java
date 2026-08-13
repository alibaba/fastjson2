package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
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
    }

    @Test
    public void testCreateModuleObjectReaderOnce() throws InterruptedException {
        AtomicInteger moduleCount = new AtomicInteger();
        ObjectReaderProvider provider = new ObjectReaderProvider();
        provider.register(new ObjectReaderModule() {
            @Override
            public ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
                if (type != ModuleBean.class) {
                    return null;
                }

                moduleCount.incrementAndGet();
                sleep(100);
                return ObjectReaderImplString.INSTANCE;
            }
        });

        Set<ObjectReader> readers = getReaders(provider, ModuleBean.class, false, 32);
        assertEquals(1, readers.size());
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
        ObjectReaderCreator creator = new ObjectReaderCreator() {
            @Override
            public <T> ObjectReader<T> createObjectReader(
                    Class<T> objectClass,
                    Type objectType,
                    boolean fieldBased,
                    ObjectReaderProvider provider
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
                    return super.createObjectReader(objectClass, objectType, fieldBased, provider);
                } finally {
                    concurrentCount.decrementAndGet();
                }
            }
        };
        ObjectReaderProvider provider = new ObjectReaderProvider(creator);
        CountDownLatch done = new CountDownLatch(3);
        AtomicReference<Throwable> error = new AtomicReference<>();

        startDaemonThread(() -> getObjectReader(provider, FailureBean.class, new AtomicReference<>(), done));
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
        startDaemonThread(() -> getObjectReader(provider, FailureBean.class, error, done));
        sleep(100);
        failFirst.countDown();
        assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
        sleep(100);
        startDaemonThread(() -> getObjectReader(provider, FailureBean.class, error, done));
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
                sleep(1200);
                return super.createObjectReader(objectClass, objectType, fieldBased, provider);
            }
        };
        ObjectReaderProvider provider = new ObjectReaderProvider(creator);
        Set<ObjectReader> readers = getReaders(provider, Bean.class, fieldBased, 32);

        assertEquals(1, readers.size());
        assertEquals(1, createCount.get());
        return new ObjectReaderTestResult(provider, readers, createCount);
    }

    private static Set<ObjectReader> getReaders(
            ObjectReaderProvider provider,
            Type type,
            boolean fieldBased,
            int threadCount
    ) throws InterruptedException {
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
                    readers.add(provider.getObjectReader(type, fieldBased));
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
        return readers;
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
