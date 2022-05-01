package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ShortValueArrayTest {
    @Test
    public void test_parse_null() {
        short[] values = JSON.parseObject("null", short[].class);
        assertNull(values);
    }

    @Test
    public void test_parse_null_jsonb() {
        short[] values = JSONB.parseObject(JSONB.toBytes((Map) null), short[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        short[] bytes = JSON.parseObject("[101,102]", short[].class);
        assertEquals(2, bytes.length);
        assertEquals(101, bytes[0]);
        assertEquals(102, bytes[1]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{101, 102}));
        short[] array = JSONB.parseObject(jsonbBytes, short[].class);
        assertEquals(2, array.length);
        assertEquals(101, array[0]);
        assertEquals(102, array[1]);
    }
}
