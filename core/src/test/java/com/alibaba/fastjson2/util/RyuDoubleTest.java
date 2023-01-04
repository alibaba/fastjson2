package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RyuDoubleTest {
    @Test
    public void test() {
        assertEquals("NaN", RyuDouble.toString(Double.NaN));
        assertEquals("Infinity", RyuDouble.toString(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", RyuDouble.toString(Double.NEGATIVE_INFINITY));
        assertEquals("-0.0", RyuDouble.toString(-0.0));
    }

    @Test
    public void test1() {
        assertEquals("NaN", toString(Double.NaN));
        assertEquals("Infinity", toString(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", toString(Double.NEGATIVE_INFINITY));
        assertEquals("-0.0", toString(-0.0));
    }

    static String toString(double value) {
        byte[] bytes = new byte[24];
        int len = RyuDouble.toString(value, bytes, 0);
        return new String(bytes, 0, len);
    }
}
