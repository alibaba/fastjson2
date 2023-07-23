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

    float[] floats = new float[] {
            -0.0f,
            0.0f,
            Float.NaN,
            Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            123.456f,
            0,
            1,
            Float.MIN_VALUE,
            Float.MAX_VALUE
    };

    @Test
    public void test() {
        byte[] bytes = new byte[24];

        for (int i = 0; i < doubles.length; i++) {
            double d = doubles[i];
            int size = DoubleToDecimal.toString(d, bytes, 0, false);
            String str = new String(bytes, 0, size);
            assertEquals(Double.toString(d), str);
        }
    }

    @Test
    public void test_chars() {
        char[] chars = new char[24];

        for (int i = 0; i < doubles.length; i++) {
            double d = doubles[i];
            int size = DoubleToDecimal.toString(d, chars, 0, false);
            String str = new String(chars, 0, size);
            assertEquals(Double.toString(d), str);
        }
    }

    @Test
    public void testFloat() {
        byte[] bytes = new byte[24];

        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            int size = DoubleToDecimal.toString(f, bytes, 0, false);
            String str = new String(bytes, 0, size);
            assertEquals(Float.toString(f), str);
        }
    }

    @Test
    public void testFloat_chars() {
        char[] chars = new char[24];

        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            int size = DoubleToDecimal.toString(f, chars, 0, false);
            String str = new String(chars, 0, size);
            assertEquals(Float.toString(f), str);
        }
    }

    @Test
    public void testFloatNull() {
        float[] floats = new float[] {
                Float.NaN,
                Float.NEGATIVE_INFINITY,
                Float.POSITIVE_INFINITY,
        };

        byte[] bytes = new byte[24];
        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            int size = DoubleToDecimal.toString(f, bytes, 0, true);
            String str = new String(bytes, 0, size);
            assertEquals("null", str);
        }
    }

    @Test
    public void testFloatNull_chars() {
        float[] floats = new float[] {
                Float.NaN,
                Float.NEGATIVE_INFINITY,
                Float.POSITIVE_INFINITY,
        };

        char[] chars = new char[24];
        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            int size = DoubleToDecimal.toString(f, chars, 0, true);
            String str = new String(chars, 0, size);
            assertEquals("null", str);
        }
    }

    @Test
    public void testDoubleNull() {
        double[] doubles = new double[] {
                Double.NaN,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY,
        };

        byte[] bytes = new byte[24];
        for (int i = 0; i < doubles.length; i++) {
            double f = doubles[i];
            int size = DoubleToDecimal.toString(f, bytes, 0, true);
            String str = new String(bytes, 0, size);
            assertEquals("null", str);
        }
    }

    @Test
    public void test1() {
        for (int i = 0; i < doubles.length; i++) {
            double d = doubles[i];
            byte[] bytes = new byte[25];
            bytes[0] = '[';
            int size = DoubleToDecimal.toString(d, bytes, 1, false);
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
            int size = DoubleToDecimal.toString(d, bytes, 1, false);
            String str = new String(bytes, 1, size);
            assertEquals(Double.toString(d), str);
        }
    }
}
