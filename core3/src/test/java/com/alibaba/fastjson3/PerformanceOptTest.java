package com.alibaba.fastjson3;

import com.alibaba.fastjson3.util.BufferPool;
import com.alibaba.fastjson3.util.JDKUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for P1 performance optimizations.
 */
class PerformanceOptTest {
    // ==================== Buffer Pool ====================

    @Test
    void charBufferPoolReuse() {
        char[] buf1 = BufferPool.borrowCharBuffer();
        assertNotNull(buf1);
        assertEquals(4096, buf1.length);

        BufferPool.returnCharBuffer(buf1);

        char[] buf2 = BufferPool.borrowCharBuffer();
        assertSame(buf1, buf2, "should reuse the same buffer");
        BufferPool.returnCharBuffer(buf2);
    }

    @Test
    void byteBufferPoolReuse() {
        byte[] buf1 = BufferPool.borrowByteBuffer();
        assertNotNull(buf1);
        assertEquals(4096, buf1.length);

        BufferPool.returnByteBuffer(buf1);

        byte[] buf2 = BufferPool.borrowByteBuffer();
        assertSame(buf1, buf2, "should reuse the same buffer");
        BufferPool.returnByteBuffer(buf2);
    }

    @Test
    void generatorReusesBuffer() {
        // After close(), the buffer should be returned to pool
        try (JSONGenerator gen = JSONGenerator.of()) {
            gen.writeString("test");
        }
        // Subsequent generator should get the recycled buffer
        try (JSONGenerator gen = JSONGenerator.of()) {
            gen.writeString("test2");
            assertEquals("\"test2\"", gen.toString());
        }
    }

    // ==================== Fast Number Parsing ====================

    @Test
    void fastReadIntBasic() {
        try (JSONParser parser = JSONParser.of("42")) {
            assertEquals(42, parser.readInt());
        }
    }

    @Test
    void fastReadIntNegative() {
        try (JSONParser parser = JSONParser.of("-123")) {
            assertEquals(-123, parser.readInt());
        }
    }

    @Test
    void fastReadIntZero() {
        try (JSONParser parser = JSONParser.of("0")) {
            assertEquals(0, parser.readInt());
        }
    }

    @Test
    void fastReadIntMaxValue() {
        try (JSONParser parser = JSONParser.of(String.valueOf(Integer.MAX_VALUE))) {
            assertEquals(Integer.MAX_VALUE, parser.readInt());
        }
    }

    @Test
    void fastReadIntMinValue() {
        try (JSONParser parser = JSONParser.of(String.valueOf(Integer.MIN_VALUE))) {
            assertEquals(Integer.MIN_VALUE, parser.readInt());
        }
    }

    @Test
    void fastReadLongBasic() {
        try (JSONParser parser = JSONParser.of("9876543210")) {
            assertEquals(9876543210L, parser.readLong());
        }
    }

    @Test
    void fastReadLongNegative() {
        try (JSONParser parser = JSONParser.of("-9876543210")) {
            assertEquals(-9876543210L, parser.readLong());
        }
    }

    @Test
    void fastReadLongMaxValue() {
        try (JSONParser parser = JSONParser.of(String.valueOf(Long.MAX_VALUE))) {
            assertEquals(Long.MAX_VALUE, parser.readLong());
        }
    }

    @Test
    void fastReadIntInArray() {
        JSONArray arr = JSON.parseArray("[1,2,3,100,-50,0]");
        assertEquals(1, arr.getIntValue(0));
        assertEquals(100, arr.getIntValue(3));
        assertEquals(-50, arr.getIntValue(4));
        assertEquals(0, arr.getIntValue(5));
    }

    @Test
    void fastReadIntFloat() {
        // readInt with a float value should truncate
        try (JSONParser parser = JSONParser.of("3.14")) {
            assertEquals(3, parser.readInt());
        }
    }

    // ==================== Pre-encoded Field Names ====================

    public static class SimpleBean {
        public String name;
        public int value;
    }

    @Test
    void preEncodedFieldNamesWork() {
        SimpleBean bean = new SimpleBean();
        bean.name = "test";
        bean.value = 42;

        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"name\":"));
        assertTrue(json.contains("\"value\":"));
        assertTrue(json.contains("\"test\""));
        assertTrue(json.contains("42"));
    }

    @Test
    void preEncodedFieldNamesUTF8() {
        SimpleBean bean = new SimpleBean();
        bean.name = "hello";
        bean.value = 99;

        byte[] bytes = JSON.toJSONBytes(bean);
        String json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(json.contains("\"name\":"));
        assertTrue(json.contains("\"hello\""));
    }

    // ==================== JDKUtils ====================

    @Test
    void unsafeAvailable() {
        assertTrue(JDKUtils.UNSAFE_AVAILABLE, "Unsafe should be available on standard JVMs");
    }

    @Test
    void jdkVersion() {
        assertTrue(JDKUtils.JDK_VERSION >= 17, "JDK version should be 17+");
    }

    @Test
    void arrayEquals() {
        byte[] a = "hello world".getBytes();
        byte[] b = "hello world".getBytes();
        assertTrue(JDKUtils.arrayEquals(a, 0, b, 0, a.length));

        byte[] c = "hello earth".getBytes();
        assertFalse(JDKUtils.arrayEquals(a, 0, c, 0, a.length));
    }

    @Test
    void arrayEqualsPartial() {
        byte[] a = "hello world".getBytes();
        byte[] b = "hello earth".getBytes();
        // First 5 bytes match ("hello")
        assertTrue(JDKUtils.arrayEquals(a, 0, b, 0, 5));
        // But full arrays don't
        assertFalse(JDKUtils.arrayEquals(a, 0, b, 0, 11));
    }

    // ==================== Hash-based field matching ====================

    @Test
    void hashFieldMatchingWorks() {
        String json = "{\"name\":\"Alice\",\"value\":42}";
        SimpleBean bean = JSON.parseObject(json, SimpleBean.class);
        assertEquals("Alice", bean.name);
        assertEquals(42, bean.value);
    }

    @Test
    void hashFieldMatchingWithManyFields() {
        String json = """
                {"a":"1","b":"2","c":"3","d":"4","e":"5","f":"6","g":"7","h":"8"}
                """;
        JSONObject obj = JSON.parseObject(json);
        assertEquals("1", obj.getString("a"));
        assertEquals("8", obj.getString("h"));
    }
}
