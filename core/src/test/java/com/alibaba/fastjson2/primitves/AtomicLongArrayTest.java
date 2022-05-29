package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLongArray;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AtomicLongArrayTest {
    @Test
    public void test_parse_null() {
        AtomicLongArray values = JSON.parseObject("null", AtomicLongArray.class);
        assertNull(values);
    }

    @Test
    public void test_parse_null_jsonb() {
        AtomicLongArray values = JSONB.parseObject(JSONB.toBytes((Map) null), AtomicLongArray.class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        AtomicLongArray array = JSON.parseObject("[101,null,102]", AtomicLongArray.class);
        assertEquals(3, array.length());
        assertEquals(101, array.get(0));
        assertEquals(0, array.get(1));
        assertEquals(102, array.get(2));
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{101, null, 102}));
        AtomicLongArray array = JSONB.parseObject(jsonbBytes, AtomicLongArray.class);
        assertEquals(3, array.length());
        assertEquals(101, array.get(0));
        assertEquals(0, array.get(1));
        assertEquals(102, array.get(2));
    }
}
