package com.alibaba.fastjson3;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class AtomicTypeTest {
    // ==================== Serialization (writeAny) ====================

    @Test
    public void testWriteAtomicInteger() {
        assertEquals("123", JSON.toJSONString(new AtomicInteger(123)));
    }

    @Test
    public void testWriteAtomicLong() {
        assertEquals("9876543210", JSON.toJSONString(new AtomicLong(9876543210L)));
    }

    @Test
    public void testWriteAtomicBoolean() {
        assertEquals("true", JSON.toJSONString(new AtomicBoolean(true)));
        assertEquals("false", JSON.toJSONString(new AtomicBoolean(false)));
    }

    @Test
    public void testWriteAtomicReference() {
        assertEquals("\"hello\"", JSON.toJSONString(new AtomicReference<>("hello")));
        assertEquals("123", JSON.toJSONString(new AtomicReference<>(123)));
        assertEquals("null", JSON.toJSONString(new AtomicReference<>(null)));
    }

    @Test
    public void testWriteAtomicIntegerArray() {
        assertEquals("[1,2,3]", JSON.toJSONString(new AtomicIntegerArray(new int[]{1, 2, 3})));
        assertEquals("[]", JSON.toJSONString(new AtomicIntegerArray(0)));
    }

    @Test
    public void testWriteAtomicLongArray() {
        assertEquals("[100,200,300]", JSON.toJSONString(new AtomicLongArray(new long[]{100, 200, 300})));
        assertEquals("[]", JSON.toJSONString(new AtomicLongArray(0)));
    }

    // ==================== Deserialization (read) ====================

    @Test
    public void testReadAtomicInteger() {
        AtomicInteger ai = JSON.parseObject("42", AtomicInteger.class);
        assertNotNull(ai);
        assertEquals(42, ai.get());
    }

    @Test
    public void testReadAtomicLong() {
        AtomicLong al = JSON.parseObject("9876543210", AtomicLong.class);
        assertNotNull(al);
        assertEquals(9876543210L, al.get());
    }

    @Test
    public void testReadAtomicBoolean() {
        AtomicBoolean ab = JSON.parseObject("true", AtomicBoolean.class);
        assertNotNull(ab);
        assertTrue(ab.get());

        ab = JSON.parseObject("false", AtomicBoolean.class);
        assertNotNull(ab);
        assertFalse(ab.get());
    }

    @Test
    public void testReadAtomicIntegerArray() {
        AtomicIntegerArray aia = JSON.parseObject("[1,2,3]", AtomicIntegerArray.class);
        assertNotNull(aia);
        assertEquals(3, aia.length());
        assertEquals(1, aia.get(0));
        assertEquals(2, aia.get(1));
        assertEquals(3, aia.get(2));
    }

    @Test
    public void testReadAtomicLongArray() {
        AtomicLongArray ala = JSON.parseObject("[100,200,300]", AtomicLongArray.class);
        assertNotNull(ala);
        assertEquals(3, ala.length());
        assertEquals(100, ala.get(0));
        assertEquals(200, ala.get(1));
        assertEquals(300, ala.get(2));
    }

    @Test
    public void testReadNull() {
        assertNull(JSON.parseObject("null", AtomicInteger.class));
        assertNull(JSON.parseObject("null", AtomicLong.class));
        assertNull(JSON.parseObject("null", AtomicBoolean.class));
        assertNull(JSON.parseObject("null", AtomicIntegerArray.class));
        assertNull(JSON.parseObject("null", AtomicLongArray.class));
    }

    // ==================== Roundtrip ====================

    @Test
    public void testRoundtripAtomicInteger() {
        AtomicInteger original = new AtomicInteger(42);
        String json = JSON.toJSONString(original);
        AtomicInteger parsed = JSON.parseObject(json, AtomicInteger.class);
        assertEquals(original.get(), parsed.get());
    }

    @Test
    public void testRoundtripAtomicLong() {
        AtomicLong original = new AtomicLong(Long.MAX_VALUE);
        String json = JSON.toJSONString(original);
        AtomicLong parsed = JSON.parseObject(json, AtomicLong.class);
        assertEquals(original.get(), parsed.get());
    }

    @Test
    public void testRoundtripAtomicIntegerArray() {
        AtomicIntegerArray original = new AtomicIntegerArray(new int[]{10, 20, 30});
        String json = JSON.toJSONString(original);
        AtomicIntegerArray parsed = JSON.parseObject(json, AtomicIntegerArray.class);
        assertEquals(original.length(), parsed.length());
        for (int i = 0; i < original.length(); i++) {
            assertEquals(original.get(i), parsed.get(i));
        }
    }

    // ==================== POJO fields ====================

    public static class Bean {
        public AtomicInteger atomicInt;
        public AtomicLong atomicLong;
        public AtomicBoolean atomicBool;
    }

    @Test
    public void testPojoWithAtomicFields() {
        Bean bean = new Bean();
        bean.atomicInt = new AtomicInteger(1);
        bean.atomicLong = new AtomicLong(2L);
        bean.atomicBool = new AtomicBoolean(true);

        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"atomicInt\":1"));
        assertTrue(json.contains("\"atomicLong\":2"));
        assertTrue(json.contains("\"atomicBool\":true"));
    }

    @Test
    public void testPojoDeserializeAtomicFields() {
        String json = "{\"atomicInt\":42,\"atomicLong\":100,\"atomicBool\":true}";
        Bean bean = JSON.parseObject(json, Bean.class);
        assertNotNull(bean);
        assertNotNull(bean.atomicInt);
        assertEquals(42, bean.atomicInt.get());
        assertNotNull(bean.atomicLong);
        assertEquals(100L, bean.atomicLong.get());
        assertNotNull(bean.atomicBool);
        assertTrue(bean.atomicBool.get());
    }

    // ==================== UTF-8 byte[] path ====================

    @Test
    public void testWriteAtomicIntegerToBytes() {
        byte[] bytes = JSON.toJSONBytes(new AtomicInteger(99));
        assertEquals("99", new String(bytes));
    }

    @Test
    public void testReadAtomicIntegerFromBytes() {
        byte[] bytes = "55".getBytes();
        AtomicInteger ai = ObjectMapper.shared().readValue(bytes, AtomicInteger.class);
        assertNotNull(ai);
        assertEquals(55, ai.get());
    }
}
