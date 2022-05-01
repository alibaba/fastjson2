package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ByteValueArrayTest {
    @Test
    public void test_parse_null() {
        byte[] bytes = JSON.parseObject("null", byte[].class);
        assertNull(bytes);
    }

    @Test
    public void test_parse_null_jsonb() {
        byte[] values = JSONB.parseObject(JSONB.toBytes((Map) null), byte[].class);
        assertNull(values);
    }

    @Test
    public void test_parse() {
        byte[] bytes = JSON.parseObject("[101,102]", byte[].class);
        assertEquals(2, bytes.length);
        assertEquals(101, bytes[0]);
        assertEquals(102, bytes[1]);
    }

    @Test
    public void test_parse_jsonb() {
        byte[] jsonbBytes = JSONB.toBytes(Arrays.asList(new Integer[]{101, 102}));
        byte[] chars = JSONB.parseObject(jsonbBytes, byte[].class);
        assertEquals(2, chars.length);
        assertEquals(101, chars[0]);
        assertEquals(102, chars[1]);
    }
}
