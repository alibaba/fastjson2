package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatToDecimalTest {
    float[] floats = new float[] {
            -0.0F,
            0.0F,
            Float.NaN,
            Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            123.456f,
            -123.456f,
            0f,
            1f,
            123f,
            -123f,
            Float.MIN_VALUE,
            Float.MAX_VALUE
    };

    @Test
    public void test() {
        byte[] bytes = new byte[15];
        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            int size = DoubleToDecimal.toString(f, bytes, 0, false);
            String str = new String(bytes, 0, size);
            assertEquals(Float.toString(f), str);
        }
    }

    @Test
    public void test1() {
        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            byte[] bytes = new byte[16];
            bytes[0] = '[';
            int size = DoubleToDecimal.toString(f, bytes, 1, false);
            String str = new String(bytes, 1, size);
            assertEquals(Float.toString(f), str);
        }
    }

    @Test
    public void test1_chars() {
        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            char[] bytes = new char[16];
            bytes[0] = '[';
            int size = DoubleToDecimal.toString(f, bytes, 1, false);
            String str = new String(bytes, 1, size);
            assertEquals(Float.toString(f), str);
        }
    }
}
