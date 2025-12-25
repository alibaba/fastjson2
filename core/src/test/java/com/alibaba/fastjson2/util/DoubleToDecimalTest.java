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
            int size = NumberUtils.writeDouble(bytes, 0, d, false, false);
            String str = new String(bytes, 0, size);
            assertEquals(d, Double.parseDouble(str));
        }
    }

    @Test
    public void test_chars() {
        char[] chars = new char[24];

        for (int i = 0; i < doubles.length; i++) {
            double d = doubles[i];
            int size = NumberUtils.writeDouble(chars, 0, d, false, false);
            String str = new String(chars, 0, size);
            assertEquals(d, Double.parseDouble(str));
        }
    }

    @Test
    public void testFloat() {
        byte[] bytes = new byte[24];

        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            int size = NumberUtils.writeFloat(bytes, 0, f, false, false);
            String str = new String(bytes, 0, size);
            assertEquals(Float.toString(f), str);
        }
    }

    @Test
    public void testFloat_chars() {
        char[] chars = new char[24];

        for (int i = 0; i < floats.length; i++) {
            float f = floats[i];
            int size = NumberUtils.writeFloat(chars, 0, f, false, false);
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
            int size = NumberUtils.writeFloat(bytes, 0, f, true, false);
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
            int size = NumberUtils.writeFloat(chars, 0, f, true, false);
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
            int size = NumberUtils.writeDouble(bytes, 0, f, true, false);
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
            int size = NumberUtils.writeDouble(bytes, 1, d, false, false) - 1;
            String str = new String(bytes, 1, size);
            assertEquals(d, Double.parseDouble(str), str);
        }
    }

    @Test
    public void test1_chars() {
        for (int i = 0; i < doubles.length; i++) {
            double d = doubles[i];
            char[] bytes = new char[25];
            bytes[0] = '[';
            int size = NumberUtils.writeDouble(bytes, 1, d, false, false) - 1;
            String str = new String(bytes, 1, size);
            assertEquals(d, Double.parseDouble(str), str);
        }
    }
}
