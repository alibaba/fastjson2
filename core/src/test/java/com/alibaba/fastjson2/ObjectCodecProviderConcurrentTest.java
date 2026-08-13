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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectCodecProviderConcurrentTest {
    @Test
    public void testCrossProviderNestedCreationDoesNotDeadlock() throws InterruptedException {
        CountDownLatch creating = new CountDownLatch(2);
        ThreadLocal<Boolean> nested = new ThreadLocal<>();
        AtomicReference<ObjectReaderProvider> readerProviderRef = new AtomicReference<>();
        AtomicReference<ObjectWriterProvider> writerProviderRef = new AtomicReference<>();

        ObjectReaderCreator readerCreator = new ObjectReaderCreator() {
            @Override
            public <T> ObjectReader<T> createObjectReader(
                    Class<T> objectClass,
                    Type objectType,
                    boolean fieldBased,
                    ObjectReaderProvider provider
            ) {
                if (objectClass == ReaderBean.class && nested.get() == null) {
                    nested.set(Boolean.TRUE);
                    try {
                        awaitConcurrentCreation(creating);
                        writerProviderRef.get().getObjectWriter(WriterBean.class);
                    } finally {
                        nested.remove();
                    }
                }
                return super.createObjectReader(objectClass, objectType, fieldBased, provider);
            }
        };
        ObjectWriterCreator writerCreator = new ObjectWriterCreator() {
            @Override
            public ObjectWriter createObjectWriter(
                    Class objectClass,
                    long features,
                    ObjectWriterProvider provider
            ) {
                if (objectClass == WriterBean.class && nested.get() == null) {
                    nested.set(Boolean.TRUE);
                    try {
                        awaitConcurrentCreation(creating);
                        readerProviderRef.get().getObjectReader(ReaderBean.class);
                    } finally {
                        nested.remove();
                    }
                }
                return super.createObjectWriter(objectClass, features, provider);
            }
        };

        ObjectReaderProvider readerProvider = new ObjectReaderProvider(readerCreator);
        ObjectWriterProvider writerProvider = new ObjectWriterProvider(writerCreator);
        readerProviderRef.set(readerProvider);
        writerProviderRef.set(writerProvider);

        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(2);
        startDaemonThread(() -> {
            try {
                readerProvider.getObjectReader(ReaderBean.class);
            } catch (Throwable e) {
                error.compareAndSet(null, e);
            } finally {
                done.countDown();
            }
        });
        startDaemonThread(() -> {
            try {
                writerProvider.getObjectWriter(WriterBean.class);
            } catch (Throwable e) {
                error.compareAndSet(null, e);
            } finally {
                done.countDown();
            }
        });

        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
    }

    private static void awaitConcurrentCreation(CountDownLatch creating) {
        creating.countDown();
        try {
            if (!creating.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("reader and writer were not created concurrently");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void startDaemonThread(Runnable task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public static class ReaderBean {
    }

    public static class WriterBean {
    }
}
