package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RyuTest {
    @Test
    public void test_toString() {
        assertEquals("1.0", toString(1F));
        assertEquals("3.4028235E38", toString(Float.MAX_VALUE));
        assertEquals("1.4E-45", toString(Float.MIN_VALUE));
        assertEquals("1.0", toString(1D));
        assertEquals("1.7976931348623157E308", toString(Double.MAX_VALUE));
        assertEquals("4.9E-324", toString(Double.MIN_VALUE));
    }

    static String toString(double value) {
        byte[] bytes = new byte[24];
        int len = DoubleToDecimal.toString(value, bytes, 0);
        return new String(bytes, 0, len);
    }

    static String toString(float value) {
        byte[] bytes = new byte[15];
        int len = DoubleToDecimal.toString(value, bytes, 0);
        return new String(bytes, 0, len);
    }
}
