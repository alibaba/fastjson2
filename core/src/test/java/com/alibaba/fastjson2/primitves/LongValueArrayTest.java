package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LongValueArrayTest {
    @Test
    public void test_parse_null() {
        long[] values = JSON.parseObject("null", long[].class);
        assertNull(values);
    }

    @Test
    public void test_parse_null_jsonb() {
        long[] values = JSONB.parseObject(JSONB.toBytes((Object) null), long[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        long[] bytes = JSON.parseObject("[101,102]", long[].class);
        assertEquals(2, bytes.length);
        assertEquals(101, bytes[0]);
        assertEquals(102, bytes[1]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{101, 102}));
        long[] array = JSONB.parseObject(jsonbBytes, long[].class);
        assertEquals(2, array.length);
        assertEquals(101, array[0]);
        assertEquals(102, array[1]);
    }
}
