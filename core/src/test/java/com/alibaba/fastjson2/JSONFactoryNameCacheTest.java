package com.alibaba.fastjson2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.alibaba.fastjson2.JSONFactory.NAME_CACHE;
import static com.alibaba.fastjson2.JSONFactory.NAME_CACHE2;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("util")
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

    @ParameterizedTest
    @ValueSource(ints = {17, 22, 31, 32})
    void longAsciiNameReused(int len) {
        String name = asciiName(len);
        byte[] bytes = jsonBytes(name, 123);
        JSONObject one = JSON.parseObject(bytes);
        JSONObject two = JSON.parseObject(bytes);
        int expectedValue = 123;
        assertEquals(expectedValue, one.getIntValue(name));
        assertEquals(expectedValue, two.getIntValue(name));
        assertSame(one.keySet().iterator().next(), two.keySet().iterator().next());
    }

    @Test
    void shortAsciiNamesStillReused() {
        Arrays.fill(NAME_CACHE, null);
        Arrays.fill(NAME_CACHE2, null);
        int[] lengths = {8, 16};
        for (int len : lengths) {
            String name = asciiName(len);
            byte[] bytes = jsonBytes(name, 456);
            JSONObject one = JSON.parseObject(bytes);
            JSONObject two = JSON.parseObject(bytes);
            int expectedValue = 456;
            assertEquals(expectedValue, one.getIntValue(name));
            assertEquals(expectedValue, two.getIntValue(name));
            assertSame(one.keySet().iterator().next(), two.keySet().iterator().next());
        }
    }

    @Test
    void boundaryAndEscapedNamesKeepValues() {
        String[] names = {asciiName(33), "abcdefghijklmnop你好", "abcdefghijklmnop\"",
                "abcdefghijklmnop\\", "abcdefghijklmnop" + (char) 0};
        for (String name : names) {
            assertDecodedName(jsonBytes(name, 111), name, 111);
        }
        String encoded = JSON.toJSONString("abcdefghijklmnopA").replace("A", "\\" + "u0041");
        assertDecodedName(("{" + encoded + ":222}").getBytes(StandardCharsets.UTF_8), "abcdefghijklmnopA", 222);
    }

    @Test
    void differentMiddleBytesKeepDistinctKeys() {
        String name1 = "prefixABmiddle01suffixYZ";
        String name2 = "prefixABmiddle02suffixYZ";
        String name3 = "prefixABmiddle01middle01suffixYZ";
        String name4 = "prefixABmiddle02middle02suffixYZ";

        for (int i = 0; i < 50; i++) {
            JSONObject obj = new JSONObject();
            obj.put(name1, 1);
            obj.put(name2, 2);
            obj.put(name3, 3);
            obj.put(name4, 4);
            byte[] bytes = obj.toString().getBytes(StandardCharsets.UTF_8);
            JSONObject parsed = JSON.parseObject(bytes);
            assertEquals(4, parsed.size());
            assertEquals(1, parsed.getIntValue(name1));
            assertEquals(2, parsed.getIntValue(name2));
            assertEquals(3, parsed.getIntValue(name3));
            assertEquals(4, parsed.getIntValue(name4));
        }
    }

    @Test
    void offsetAndReusedBufferKeepValues() {
        String name1 = asciiName(17);
        String name2 = asciiName(18);
        int val1 = 100;
        int val2 = 200;
        byte[] jsonBytes1 = jsonBytes(name1, val1);
        byte[] jsonBytes2 = jsonBytes(name2, val2);

        for (int i = 0; i < 100; i++) {
            byte[] buffer = new byte[256];
            Arrays.fill(buffer, (byte) 'x');
            int offset = 5;
            System.arraycopy(jsonBytes1, 0, buffer, offset, jsonBytes1.length);
            JSONObject one = JSON.parseObject(buffer, offset, jsonBytes1.length, StandardCharsets.UTF_8, JSONObject.class);
            assertEquals(val1, one.getIntValue(name1));

            System.arraycopy(jsonBytes2, 0, buffer, offset, jsonBytes2.length);
            JSONObject two = JSON.parseObject(buffer, offset, jsonBytes2.length, StandardCharsets.UTF_8, JSONObject.class);
            assertEquals(val2, two.getIntValue(name2));
        }
    }

    @Test
    void concurrentLongNamesKeepValues() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        Future<?>[] futures = new Future<?>[8];
        try {
            for (int t = 0; t < 8; t++) {
                futures[t] = executor.submit(() -> {
                    for (int i = 0; i < 1000; i++) {
                        String name = "nameWithLongPrefix" + (i % 100);
                        int value = i;
                        byte[] bytes = jsonBytes(name, value);
                        JSONObject obj = JSON.parseObject(bytes);
                        assertEquals(value, obj.getIntValue(name));
                    }
                });
            }
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static String asciiName(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        return sb.toString();
    }

    private static byte[] jsonBytes(String name, int value) {
        return ("{" + JSON.toJSONString(name) + ":" + value + "}").getBytes(StandardCharsets.UTF_8);
    }

    private static void assertDecodedName(byte[] jsonBytes, String name, int expectedValue) {
        JSONObject one = JSON.parseObject(jsonBytes);
        JSONObject two = JSON.parseObject(jsonBytes);
        assertEquals(expectedValue, one.getIntValue(name));
        assertEquals(expectedValue, two.getIntValue(name));
    }

    @ParameterizedTest
    @ValueSource(ints = {17, 22, 31, 32})
    void directUtf8ReaderReusesName(int len) {
        char[] chars = new char[len];
        Arrays.fill(chars, 'a');
        String name = new String(chars);
        byte[] data = ("{\"" + name + "\":123}").getBytes(StandardCharsets.UTF_8);
        String key1 = directUtf8Name(data);
        String key2 = directUtf8Name(data);
        assertEquals(name, key1);
        assertEquals(name, key2);
        assertSame(key1, key2);
    }

    private String directUtf8Name(byte[] data) {
        try (JSONReader reader = JSONReader.of(data, 0, data.length, JSONFactory.createReadContext())) {
            assertTrue(reader.nextIfObjectStart());
            String key = reader.readFieldName();
            int value = reader.readInt32Value();
            assertEquals(123, value);
            assertTrue(reader.nextIfObjectEnd());
            return key;
        }
    }

    @Test
    void latin1NonAsciiNamesRemainCorrect() {
        String name = "abcdefghijklmnopé";
        String json = "{" + JSON.toJSONString(name) + ":123}";
        byte[] isoBytes = json.getBytes(StandardCharsets.ISO_8859_1);
        JSONObject obj1 = JSON.parseObject(json, JSONObject.class);
        JSONObject obj2 = JSON.parseObject(isoBytes, 0, isoBytes.length, StandardCharsets.ISO_8859_1, JSONObject.class);
        assertEquals(123, obj1.getIntValue(name));
        assertEquals(123, obj2.getIntValue(name));
        assertNotSame(obj1.keySet().iterator().next(), obj2.keySet().iterator().next());
    }

    @ParameterizedTest
    @ValueSource(ints = {17, 24, 25, 32})
    void iso8859_1LongAsciiNamesReused(int len) {
        String name = asciiName(len);
        byte[] bytes = jsonBytes(name, 123);
        JSONObject one = JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1, JSONObject.class);
        JSONObject two = JSON.parseObject(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1, JSONObject.class);
        assertEquals(123, one.getIntValue(name));
        assertEquals(123, two.getIntValue(name));
        assertSame(one.keySet().iterator().next(), two.keySet().iterator().next());
    }

    @Test
    void latin1DistinctNamesRemainDistinct() {
        String name1 = "prefixABmiddle01suffixYZ";
        String name2 = "prefixABmiddle02suffixYZ";
        byte[] isoBytes = ("{" + JSON.toJSONString(name1) + ":1," + JSON.toJSONString(name2) + ":2}").getBytes(StandardCharsets.ISO_8859_1);
        for (int i = 0; i < 20; i++) {
            JSONObject obj = JSON.parseObject(isoBytes, 0, isoBytes.length, StandardCharsets.ISO_8859_1, JSONObject.class);
            assertEquals(2, obj.size());
            assertEquals(1, obj.getIntValue(name1));
            assertEquals(2, obj.getIntValue(name2));
        }
    }

    @Test
    void offsetAndReusedBufferIso8859_1KeepValues() {
        String name1 = asciiName(17);
        String name2 = asciiName(24);
        int val1 = 100;
        int val2 = 200;
        byte[] jsonBytes1 = jsonBytes(name1, val1);
        byte[] jsonBytes2 = jsonBytes(name2, val2);
        byte[] buffer = new byte[256];
        Arrays.fill(buffer, (byte) 'x');
        int offset = 5;
        System.arraycopy(jsonBytes1, 0, buffer, offset, jsonBytes1.length);
        JSONObject one = JSON.parseObject(buffer, offset, jsonBytes1.length, StandardCharsets.ISO_8859_1, JSONObject.class);
        assertEquals(val1, one.getIntValue(name1));
        assertEquals(1, one.size());

        Arrays.fill(buffer, (byte) 'x');
        System.arraycopy(jsonBytes2, 0, buffer, offset, jsonBytes2.length);
        JSONObject two = JSON.parseObject(buffer, offset, jsonBytes2.length, StandardCharsets.ISO_8859_1, JSONObject.class);
        assertEquals(val2, two.getIntValue(name2));

        assertEquals(val1, one.getIntValue(name1));
        assertEquals(1, one.size());
        assertNotSame(one.keySet().iterator().next(), two.keySet().iterator().next());
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
