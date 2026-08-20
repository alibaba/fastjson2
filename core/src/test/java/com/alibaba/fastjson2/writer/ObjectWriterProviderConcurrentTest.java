package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.internal.CodecCreationCoordinator;
import com.alibaba.fastjson2.internal.CodecCreationCoordinatorTestSupport;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
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
        assertNoCreateLocks(result.provider);
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
        assertNoCreateLocks(provider);
    }

    @Test
    public void testSpecializedObjectWriterIsPublished() throws InterruptedException {
        ObjectWriterProvider provider = new ObjectWriterProvider();

        Set<ObjectWriter> writers = getWriters(provider, ExtendedMap.class, false, 32);

        assertEquals(1, writers.size());
        assertSame(writers.iterator().next(), provider.cache.get(ExtendedMap.class));
        assertNoCreateLocks(provider);
    }

    @Test
    public void testCreatorFailureDoesNotReplaceLiveLock() throws InterruptedException {
        AtomicInteger createCount = new AtomicInteger();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch failFirst = new CountDownLatch(1);
        IllegalStateException expectedError = new IllegalStateException("first creation failed");
        ObjectWriterCreator creator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                if (createCount.incrementAndGet() == 1) {
                    firstStarted.countDown();
                    await(failFirst);
                    throw expectedError;
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        CountDownLatch done = new CountDownLatch(2);
        AtomicReference<Throwable> firstError = new AtomicReference<>();
        AtomicReference<Throwable> secondError = new AtomicReference<>();

        startDaemonThread(() -> getObjectWriter(provider, FailureBean.class, firstError, done));
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
        Thread second = startDaemonThread(() -> getObjectWriter(provider, FailureBean.class, secondError, done));
        awaitWaiting(second);
        failFirst.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));

        assertSame(expectedError, firstError.get());
        assertSame(expectedError, secondError.get());
        assertEquals(1, createCount.get());

        CountDownLatch retryDone = new CountDownLatch(1);
        AtomicReference<ObjectWriter> retryWriter = new AtomicReference<>();
        AtomicReference<Throwable> retryError = new AtomicReference<>();
        startDaemonThread(() -> {
            try {
                retryWriter.set(provider.getObjectWriter(FailureBean.class));
            } catch (Throwable e) {
                retryError.set(e);
            } finally {
                retryDone.countDown();
            }
        });

        assertTrue(retryDone.await(5, TimeUnit.SECONDS));
        assertNull(retryError.get());
        assertNotNull(retryWriter.get());
        assertEquals(2, createCount.get());
        assertNoCreateLocks(provider);
    }

    @Test
    public void testCreatorFailureDoesNotLeakWhileFallbackRetainsLockEntry() throws InterruptedException {
        long waitNanos = TimeUnit.MILLISECONDS.toNanos(10);
        AtomicInteger createCount = new AtomicInteger();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch failFirst = new CountDownLatch(1);
        IllegalStateException expectedError = new IllegalStateException("first creation failed");
        ObjectWriterCreator creator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                if (createCount.incrementAndGet() == 1) {
                    firstStarted.countDown();
                    await(failFirst);
                    throw expectedError;
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        CountDownLatch firstDone = new CountDownLatch(1);
        AtomicReference<Throwable> firstError = new AtomicReference<>();
        startDaemonThread(() -> getObjectWriter(provider, FailureBean.class, firstError, firstDone));
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));

        CountDownLatch fallbackEntered = new CountDownLatch(1);
        CountDownLatch releaseFallback = new CountDownLatch(1);
        CountDownLatch fallbackDone = new CountDownLatch(1);
        AtomicReference<Throwable> fallbackError = new AtomicReference<>();
        startDaemonThread(() -> {
            try (CodecCreationCoordinator.Scope fallback =
                         CodecCreationCoordinatorTestSupport.acquire(
                                 provider.createLocks,
                                 FailureBean.class,
                                 waitNanos,
                                 0
                         )) {
                assertTrue(fallback.isLockFreeFallback());
                fallbackEntered.countDown();
                await(releaseFallback);
            } catch (Throwable error) {
                fallbackError.set(error);
            } finally {
                fallbackDone.countDown();
            }
        });

        AtomicReference<ObjectWriter> retryWriter = new AtomicReference<>();
        AtomicReference<Throwable> retryError = new AtomicReference<>();
        CountDownLatch retryDone = new CountDownLatch(1);
        try {
            assertTrue(fallbackEntered.await(5, TimeUnit.SECONDS));
            failFirst.countDown();
            assertTrue(firstDone.await(5, TimeUnit.SECONDS));

            startDaemonThread(() -> {
                try {
                    retryWriter.set(provider.getObjectWriter(FailureBean.class));
                } catch (Throwable error) {
                    retryError.set(error);
                } finally {
                    retryDone.countDown();
                }
            });
            assertTrue(retryDone.await(5, TimeUnit.SECONDS));
        } finally {
            failFirst.countDown();
            releaseFallback.countDown();
        }

        assertTrue(fallbackDone.await(5, TimeUnit.SECONDS));
        assertSame(expectedError, firstError.get());
        assertNull(fallbackError.get());
        assertNull(retryError.get());
        assertNotNull(retryWriter.get());
        assertEquals(2, createCount.get());
        assertNoCreateLocks(provider);
    }

    @Test
    public void testNestedCreationWaitsWhenThereIsNoCycle() throws InterruptedException {
        AtomicInteger beanBCreateCount = new AtomicInteger();
        CountDownLatch beanBStarted = new CountDownLatch(1);
        CountDownLatch finishBeanB = new CountDownLatch(1);
        ObjectWriterCreator creator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                if (objectClass == BeanB.class) {
                    beanBCreateCount.incrementAndGet();
                    beanBStarted.countDown();
                    await(finishBeanB);
                } else if (objectClass == BeanA.class) {
                    await(beanBStarted);
                    provider.getObjectWriter(BeanB.class);
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(2);

        startDaemonThread(() -> getObjectWriter(provider, BeanB.class, error, done));
        assertTrue(beanBStarted.await(5, TimeUnit.SECONDS));
        Thread beanAThread = startDaemonThread(() -> getObjectWriter(provider, BeanA.class, error, done));
        awaitWaiting(beanAThread);

        assertEquals(1, beanBCreateCount.get());
        finishBeanB.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(1, beanBCreateCount.get());
        assertNoCreateLocks(provider);
    }

    @Test
    public void testNestedCreationDoesNotDeadlock() throws InterruptedException {
        CountDownLatch creating = new CountDownLatch(2);
        CountDownLatch fallbackStarted = new CountDownLatch(1);
        CountDownLatch continueFallback = new CountDownLatch(1);
        ThreadLocal<Boolean> nested = new ThreadLocal<>();
        AtomicReference<Class<?>> fallbackType = new AtomicReference<>();
        AtomicReference<ObjectWriter> nestedWriterA = new AtomicReference<>();
        AtomicReference<ObjectWriter> nestedWriterB = new AtomicReference<>();
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
                        Class<?> nestedType = objectClass == BeanA.class ? BeanB.class : BeanA.class;
                        ObjectWriter nestedWriter = provider.getObjectWriter(nestedType);
                        if (nestedType == BeanA.class) {
                            nestedWriterA.set(nestedWriter);
                        } else {
                            nestedWriterB.set(nestedWriter);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    } finally {
                        nested.remove();
                    }
                } else if (objectClass == BeanA.class || objectClass == BeanB.class) {
                    if (fallbackType.compareAndSet(null, objectClass)) {
                        fallbackStarted.countDown();
                    }
                    await(continueFallback);
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        ObjectWriter canonical = ObjectWriterImplString.INSTANCE;
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<ObjectWriter> writerA = new AtomicReference<>();
        AtomicReference<ObjectWriter> writerB = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(2);

        startDaemonThread(() -> getObjectWriter(provider, BeanA.class, writerA, error, done));
        startDaemonThread(() -> getObjectWriter(provider, BeanB.class, writerB, error, done));

        try {
            assertTrue(fallbackStarted.await(5, TimeUnit.SECONDS));
            assertNotNull(fallbackType.get());
            provider.register(fallbackType.get(), canonical);
        } finally {
            continueFallback.countDown();
        }
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
        assertSame(canonical, provider.getObjectWriter(fallbackType.get()));
        assertSame(provider.getObjectWriter(BeanA.class), writerA.get());
        assertSame(provider.getObjectWriter(BeanB.class), writerB.get());
        assertSame(provider.getObjectWriter(BeanA.class), nestedWriterA.get());
        assertSame(provider.getObjectWriter(BeanB.class), nestedWriterB.get());
        assertNoCreateLocks(provider);
    }

    @Test
    public void testFallbackOwnerSameTypeReentryDoesNotDeadlock() throws InterruptedException {
        AtomicInteger createCount = new AtomicInteger();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch fallbackStarted = new CountDownLatch(1);
        ThreadLocal<Boolean> nested = new ThreadLocal<>();
        AtomicReference<ObjectWriter> nestedWriter = new AtomicReference<>();
        ObjectWriterCreator creator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                if (objectClass == Bean.class) {
                    int count = createCount.incrementAndGet();
                    if (count == 1) {
                        firstStarted.countDown();
                        await(releaseFirst);
                    } else if (nested.get() == null) {
                        nested.set(Boolean.TRUE);
                        try {
                            fallbackStarted.countDown();
                            nestedWriter.set(provider.getObjectWriter(Bean.class));
                        } finally {
                            nested.remove();
                        }
                    }
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };
        ObjectWriterProvider provider = new ObjectWriterProvider(creator);
        AtomicReference<ObjectWriter> firstWriter = new AtomicReference<>();
        AtomicReference<ObjectWriter> fallbackWriter = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch firstDone = new CountDownLatch(1);
        CountDownLatch fallbackDone = new CountDownLatch(1);

        startDaemonThread(() -> getObjectWriter(provider, Bean.class, firstWriter, error, firstDone));
        boolean fallbackEntered;
        boolean completedWhileFirstHeld;
        try {
            assertTrue(firstStarted.await(5, TimeUnit.SECONDS));
            startDaemonThread(() -> getObjectWriter(provider, Bean.class, fallbackWriter, error, fallbackDone));
            fallbackEntered = fallbackStarted.await(10, TimeUnit.SECONDS);
            completedWhileFirstHeld = fallbackEntered
                    && fallbackDone.await(5, TimeUnit.SECONDS);
        } finally {
            releaseFirst.countDown();
        }

        assertTrue(firstDone.await(5, TimeUnit.SECONDS));
        assertTrue(fallbackDone.await(5, TimeUnit.SECONDS));
        assertTrue(fallbackEntered);
        assertTrue(completedWhileFirstHeld);
        assertNull(error.get());
        assertEquals(3, createCount.get());
        ObjectWriter canonical = provider.getObjectWriter(Bean.class);
        assertSame(canonical, firstWriter.get());
        assertSame(canonical, fallbackWriter.get());
        assertSame(canonical, nestedWriter.get());
        assertNoCreateLocks(provider);
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
        assertNoCreateLocks(provider);
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
        assertNoCreateLocks(provider);
        return new ObjectWriterTestResult(provider, writers, createCount);
    }

    private static void assertNoCreateLocks(ObjectWriterProvider provider) {
        assertTrue(provider.createLocks.isEmpty());
        assertTrue(provider.createLocksFieldBased.isEmpty());
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
        getObjectWriter(provider, type, null, error, done);
    }

    private static void getObjectWriter(
            ObjectWriterProvider provider,
            Type type,
            AtomicReference<ObjectWriter> result,
            AtomicReference<Throwable> error,
            CountDownLatch done
    ) {
        try {
            ObjectWriter objectWriter = provider.getObjectWriter(type);
            if (result != null) {
                result.set(objectWriter);
            }
        } catch (Throwable e) {
            error.compareAndSet(null, e);
        } finally {
            done.countDown();
        }
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

    private static Thread startDaemonThread(Runnable task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        return thread;
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

    public static class ExtendedMap extends HashMap<String, String> {
        private final String name;

        public ExtendedMap(String ignored) {
            this.name = ignored;
        }

        public String getName() {
            return name;
        }
    }

    public static class GenericBean<T> {
        public T value;
    }

    public static class ValueA {
    }

    public static class ValueB {
    }
}
