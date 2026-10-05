package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ShortArrayNullTest {
    @Test
    public void boxedShortArraysPreserveNullElements() {
        String json = "[null,0,-32768,32767,null]";
        Short[] expected = {null, 0, Short.MIN_VALUE, Short.MAX_VALUE, null};
        assertArrayEquals(expected, JSON.parseObject(json, Short[].class));
        assertArrayEquals(expected, JSON.parseObject(json.toCharArray(), Short[].class));
        assertArrayEquals(expected, JSON.parseObject(json.getBytes(StandardCharsets.UTF_8), Short[].class));
        assertArrayEquals(expected, JSONB.parseObject(JSONB.toBytes(expected), Short[].class));
    }
}
