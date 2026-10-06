package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderCreator;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectCodecProviderLivenessTest {
    @Test
    public void testReaderExternalDependencyThroughTwoCreators() throws InterruptedException {
        ExternalDependency dependency = new ExternalDependency();
        ObjectReaderProvider provider = new ObjectReaderProvider(new ObjectReaderCreator() {
            @Override
            public <T> ObjectReader<T> createObjectReader(
                    Class<T> objectClass,
                    Type objectType,
                    boolean fieldBased,
                    ObjectReaderProvider provider
            ) {
                if (objectClass == Bean.class) {
                    dependency.enter();
                }
                return super.createObjectReader(objectClass, objectType, fieldBased, provider);
            }
        });
        dependency.check(() -> provider.getObjectReader(Bean.class));
    }

    @Test
    public void testWriterExternalDependencyThroughTwoCreators() throws InterruptedException {
        ExternalDependency dependency = new ExternalDependency();
        ObjectWriterProvider provider = new ObjectWriterProvider(new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                if (objectClass == Bean.class) {
                    dependency.enter();
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        });
        dependency.check(() -> provider.getObjectWriter(Bean.class));
    }

    private static class ExternalDependency {
        final AtomicInteger creates = new AtomicInteger();
        final CountDownLatch firstEntered = new CountDownLatch(1);
        final CountDownLatch secondEntered = new CountDownLatch(1);
        final CountDownLatch dependencyResolved = new CountDownLatch(1);

        void enter() {
            int count = creates.incrementAndGet();
            if (count == 1) {
                firstEntered.countDown();
            } else if (count == 2) {
                secondEntered.countDown();
            }
            if (count <= 2) {
                try {
                    assertTrue(dependencyResolved.await(30, TimeUnit.SECONDS));
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(error);
                }
            }
        }

        void check(Supplier<Object> lookup) throws InterruptedException {
            AtomicReference<Throwable> error = new AtomicReference<>();
            AtomicReference<Object> firstResult = new AtomicReference<>();
            AtomicReference<Object> secondResult = new AtomicReference<>();
            AtomicReference<Object> thirdResult = new AtomicReference<>();
            CountDownLatch firstDone = new CountDownLatch(1);
            CountDownLatch secondDone = new CountDownLatch(1);
            CountDownLatch thirdDone = new CountDownLatch(1);
            boolean secondStarted = false;
            boolean thirdStarted = false;
            startLookup(lookup, firstResult, error, firstDone, false);
            try {
                assertTrue(firstEntered.await(5, TimeUnit.SECONDS));
                startLookup(lookup, secondResult, error, secondDone, false);
                secondStarted = true;
                assertTrue(secondEntered.await(10, TimeUnit.SECONDS));
                startLookup(lookup, thirdResult, error, thirdDone, true);
                thirdStarted = true;
                // Both creators are waiting for this lookup, not for a test-thread release.
                assertTrue(thirdDone.await(10, TimeUnit.SECONDS));
                assertNull(error.get());
                assertEquals(0, dependencyResolved.getCount());
            } finally {
                dependencyResolved.countDown();
                assertTrue(firstDone.await(5, TimeUnit.SECONDS));
                if (secondStarted) {
                    assertTrue(secondDone.await(5, TimeUnit.SECONDS));
                }
                if (thirdStarted) {
                    assertTrue(thirdDone.await(5, TimeUnit.SECONDS));
                }
            }
            assertNull(error.get());
            assertEquals(3, creates.get());
            assertSame(thirdResult.get(), firstResult.get());
            assertSame(thirdResult.get(), secondResult.get());
            assertSame(thirdResult.get(), lookup.get());
        }

        void startLookup(
                Supplier<Object> lookup,
                AtomicReference<Object> result,
                AtomicReference<Throwable> error,
                CountDownLatch done,
                boolean resolvesDependency
        ) {
            Thread thread = new Thread(() -> {
                try {
                    result.set(lookup.get());
                    if (resolvesDependency) {
                        dependencyResolved.countDown();
                    }
                } catch (Throwable failure) {
                    error.compareAndSet(null, failure);
                } finally {
                    done.countDown();
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    public static class Bean {
        public int id;
    }
}
