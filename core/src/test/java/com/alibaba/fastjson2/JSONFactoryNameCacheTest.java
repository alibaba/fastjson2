package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson2.JSONFactory.NAME_CACHE;
import static com.alibaba.fastjson2.JSONFactory.NAME_CACHE2;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JSONFactoryNameCacheTest {
    @Test
    public void test() throws Exception {
        Arrays.fill(NAME_CACHE, 0, NAME_CACHE.length, null);
        Arrays.fill(NAME_CACHE2, 0, NAME_CACHE2.length, null);

        final AtomicLong errorCount = new AtomicLong();
        Random r = new Random();
        ExecutorService executor = Executors.newFixedThreadPool(16);

        final int COUNT = 100000;
        CountDownLatch latch = new CountDownLatch(COUNT);
        for (int j = 0; j < COUNT; j++) {
            int len = r.nextInt(16);
            byte[] bytes = new byte[len];
            for (int i = 0; i < len; i++) {
                bytes[i] = (byte) r.nextInt(127);
            }

            String str = new String(bytes, 0, len);
            executor.submit(new Task(str, latch, errorCount));
        }

        latch.await();
    }

    public static class Task
            implements Runnable {
        final CountDownLatch latch;
        final AtomicLong errorCount;
        final String str;

        public Task(String str, CountDownLatch latch, AtomicLong errorCount) {
            this.str = str;
            this.latch = latch;
            this.errorCount = errorCount;
        }

        @Override
        public void run() {
            try {
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.startObject();
                jsonWriter.writeName(str);
                jsonWriter.writeInt32(1);
                jsonWriter.endObject();

                byte[] jsonbBytes = jsonWriter.getBytes();
                JSONReader reader = JSONReader.ofJSONB(jsonbBytes);
                assertTrue(reader.nextIfObjectStart());
                String str1 = reader.readFieldName();
                if (!str.equals(str1)) {
                    errorCount.incrementAndGet();
                    System.err.println(str);
                }
            } finally {
                latch.countDown();
            }
        }
    }
}
