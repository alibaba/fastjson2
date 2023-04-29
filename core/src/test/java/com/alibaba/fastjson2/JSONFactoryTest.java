package com.alibaba.fastjson2;

import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static com.alibaba.fastjson2.JSONFactory.CACHE_SIZE;
import static org.junit.jupiter.api.Assertions.*;

public class JSONFactoryTest {
    @Test
    public void contextReaderCreator() {
        JSONFactory.setContextObjectReaderProvider(null);
        assertSame(JSONFactory.defaultObjectReaderProvider, JSONFactory.getDefaultObjectReaderProvider());
    }

    @Test
    public void contextJSONPathCompiler() {
        JSONFactory.setContextJSONPathCompiler(null);
        assertSame(JSONFactory.defaultJSONPathCompiler, JSONFactory.getDefaultJSONPathCompiler());
    }

    @Test
    public void test1() {
        JSONFactory.setUseJacksonAnnotation(false);
        assertFalse(JSONFactory.isUseJacksonAnnotation());
        JSONFactory.setUseJacksonAnnotation(true);
        assertTrue(JSONFactory.isUseJacksonAnnotation());

        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        JSONWriter.Context context = JSONFactory.createWriteContext(provider);
        assertSame(provider, context.getProvider());
    }

    @Test
    public void testCache() throws Exception {
        final int THREAD_CNT = 256;
        final CountDownLatch latch = new CountDownLatch(THREAD_CNT);

        Runnable task = () -> {
            int cachedIndex = System.identityHashCode(Thread.currentThread()) & (CACHE_SIZE - 1);

            char[] chars = JSONFactory.allocateCharArray(cachedIndex);
            JSONFactory.releaseCharArray(cachedIndex, chars);
            char[] chars1 = JSONFactory.allocateCharArray(cachedIndex);
            JSONFactory.releaseCharArray(cachedIndex, chars1);

            byte[] bytes = JSONFactory.allocateByteArray(cachedIndex);
            JSONFactory.releaseByteArray(cachedIndex, bytes);
            byte[] bytes1 = JSONFactory.allocateByteArray(cachedIndex);
            JSONFactory.releaseByteArray(cachedIndex, bytes1);

            latch.countDown();
        };

        Thread[] threads = new Thread[THREAD_CNT];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        latch.await();
    }
}
