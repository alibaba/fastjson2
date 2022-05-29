package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AtomicIntegerArrayTest {
    @Test
    public void test_parse_null() {
        AtomicIntegerArray values = JSON.parseObject("null", AtomicIntegerArray.class);
        assertNull(values);
    }

    @Test
    public void test_parse_null_jsonb() {
        AtomicIntegerArray values = JSONB.parseObject(JSONB.toBytes((Map) null), AtomicIntegerArray.class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        AtomicIntegerArray array = JSON.parseObject("[101,null,102]", AtomicIntegerArray.class);
        assertEquals(3, array.length());
        assertEquals(101, array.get(0));
        assertEquals(0, array.get(1));
        assertEquals(102, array.get(2));
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(
                Arrays.asList(new Integer[]{101, null, 102}));
        AtomicIntegerArray array = JSONB.parseObject(jsonbBytes, AtomicIntegerArray.class);
        assertEquals(3, array.length());
        assertEquals(101, array.get(0));
        assertEquals(0, array.get(1));
        assertEquals(102, array.get(2));
    }
}
