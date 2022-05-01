package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IntValueArrayTest {
    @Test
    public void test_parse_null() {
        int[] values = JSON.parseObject("null", int[].class);
        assertNull(values);
    }

    @Test
    public void test_parse_null_jsonb() {
        int[] values = JSONB.parseObject(JSONB.toBytes((Map) null), int[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        int[] bytes = JSON.parseObject("[101,102]", int[].class);
        assertEquals(2, bytes.length);
        assertEquals(101, bytes[0]);
        assertEquals(102, bytes[1]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{101, 102}));
        int[] array = JSONB.parseObject(jsonbBytes, int[].class);
        assertEquals(2, array.length);
        assertEquals(101, array[0]);
        assertEquals(102, array[1]);
    }
}
