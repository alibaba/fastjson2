package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RyuFloatTest {
    @Test
    public void test() {
        assertEquals("NaN", RyuFloat.toString(Float.NaN));
        assertEquals("Infinity", RyuFloat.toString(Float.POSITIVE_INFINITY));
        assertEquals("-Infinity", RyuFloat.toString(Float.NEGATIVE_INFINITY));
        assertEquals("-0.0", RyuFloat.toString(-0.0F));
    }

    @Test
    public void test1() {
        assertEquals("NaN", toString(Float.NaN));
        assertEquals("Infinity", toString(Float.POSITIVE_INFINITY));
        assertEquals("-Infinity", toString(Float.NEGATIVE_INFINITY));
        assertEquals("-0.0", toString(-0.0F));
    }

    static String toString(float value) {
        byte[] bytes = new byte[24];
        int len = RyuFloat.toString(value, bytes, 0);
        return new String(bytes, 0, len);
    }
}
