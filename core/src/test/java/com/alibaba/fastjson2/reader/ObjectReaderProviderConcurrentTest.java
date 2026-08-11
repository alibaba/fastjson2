package com.alibaba.fastjson2.reader;

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
            new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                    readers.add(provider.getObjectReader(Bean.class));
                } catch (Throwable e) {
                    error.compareAndSet(null, e);
                } finally {
                    done.countDown();
                }
            }).start();
        }

        boolean allReady = ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        assertTrue(allReady);
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(1, readers.size());
        assertEquals(1, createCount.get());
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
}
