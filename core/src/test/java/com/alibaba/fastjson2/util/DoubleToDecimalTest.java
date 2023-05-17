package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoubleToDecimalTest {
    double[] doubles = new double[] {
            -0.0,
            0.0,
            Double.NaN,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            123.456,
            0,
            1,
            Double.MIN_VALUE,
            Double.MAX_VALUE
    };

    @Test
    public void test() {
        byte[] bytes = new byte[24];

        for (int i = 0; i < doubles.length; i++) {
            double d = doubles[i];
            int size = DoubleToDecimal.toString(d, bytes, 0);
            String str = new String(bytes, 0, 0, size);
            assertEquals(Double.toString(d), str);
        }
    }

    @Test
    public void test1() {
        for (int i = 0; i < doubles.length; i++) {
            double d = doubles[i];
            byte[] bytes = new byte[25];
            bytes[0] = '[';
            int size = DoubleToDecimal.toString(d, bytes, 1);
            String str = new String(bytes, 1, size);
            assertEquals(Double.toString(d), str);
        }
    }

    @Test
    public void test1_chars() {
        for (int i = 0; i < doubles.length; i++) {
            double d = doubles[i];
            char[] bytes = new char[25];
            bytes[0] = '[';
            int size = DoubleToDecimal.toString(d, bytes, 1);
            String str = new String(bytes, 1, size);
            assertEquals(Double.toString(d), str);
        }
    }
}
