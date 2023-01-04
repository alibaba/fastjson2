package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONReaderFloatTest {
    @Test
    public void test5() {
        char[] chars = new char[]{'0', '.', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'};
        for (int i0 = 0; i0 < 2; i0++) {
            char c0 = (char) ('0' + i0);
            chars[2] = c0;

            String str0 = new String(chars, 0, 3);
            float f0 = Float.parseFloat(str0);
            double d0 = Double.parseDouble(str0);
            assertEquals(f0, JSON.parseObject(str0, Float.class));
            assertEquals(d0, JSON.parseObject(str0, Double.class));
            assertEquals(f0, ((Float) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
            assertEquals(d0, ((Double) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[3] = c1;

                String str1 = new String(chars, 0, 4);
                float f1 = Float.parseFloat(str1);
                double d1 = Double.parseDouble(str1);
                assertEquals(f1, JSON.parseObject(str1, Float.class));
                assertEquals(d1, JSON.parseObject(str1, Double.class));
                assertEquals(f1, ((Float) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                assertEquals(d1, ((Double) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[4] = c2;

                    String str2 = new String(chars, 0, 5);
                    float f2 = Float.parseFloat(str2);
                    double d2 = Double.parseDouble(str2);
                    assertEquals(f2, JSON.parseObject(str2, Float.class));
                    assertEquals(d2, JSON.parseObject(str2, Double.class));
                    assertEquals(f2, ((Float) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                    assertEquals(d2, ((Double) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[5] = c3;

                        String str3 = new String(chars, 0, 6);
                        float f3 = Float.parseFloat(str3);
                        double d3 = Double.parseDouble(str3);
                        assertEquals(f3, JSON.parseObject(str3, Float.class));
                        assertEquals(d3, JSON.parseObject(str3, Double.class));
                        assertEquals(f3, ((Float) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                        assertEquals(d3, ((Double) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                        for (int i4 = 0; i4 < 10; i4++) {
                            char c4 = (char) ('0' + i4);
                            chars[6] = c4;

                            String str4 = new String(chars, 0, 7);
                            float f4 = Float.parseFloat(str4);
                            double d4 = Double.parseDouble(str4);
                            assertEquals(f4, JSON.parseObject(str4, Float.class));
                            assertEquals(d4, JSON.parseObject(str4, Double.class));
                            assertEquals(f4, ((Float) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                            assertEquals(d4, ((Double) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void test10() {
        char[] chars = new char[]{
                '0', '.',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
        };
        int OFF = 5;
        for (int i0 = 0; i0 < 2; i0++) {
            char c0 = (char) ('0' + i0);
            chars[OFF + 2] = c0;

            String str0 = new String(chars, 0, OFF + 3);
            float f0 = Float.parseFloat(str0);
            double d0 = Double.parseDouble(str0);
            assertEquals(f0, JSON.parseObject(str0, Float.class));
            assertEquals(d0, JSON.parseObject(str0, Double.class));
            assertEquals(f0, ((Float) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
            assertEquals(d0, ((Double) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[OFF + 3] = c1;

                String str1 = new String(chars, 0, OFF + 4);
                float f1 = Float.parseFloat(str1);
                double d1 = Double.parseDouble(str1);
                assertEquals(f1, JSON.parseObject(str1, Float.class));
                assertEquals(d1, JSON.parseObject(str1, Double.class));
                assertEquals(f1, ((Float) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                assertEquals(d1, ((Double) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[OFF + 4] = c2;

                    String str2 = new String(chars, 0, OFF + 5);
                    float f2 = Float.parseFloat(str2);
                    double d2 = Double.parseDouble(str2);
                    assertEquals(f2, JSON.parseObject(str2, Float.class));
                    assertEquals(d2, JSON.parseObject(str2, Double.class));
                    assertEquals(f2, ((Float) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                    assertEquals(d2, ((Double) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[OFF + 5] = c3;

                        String str3 = new String(chars, 0, OFF + 6);
                        float f3 = Float.parseFloat(str3);
                        double d3 = Double.parseDouble(str3);
                        assertEquals(f3, JSON.parseObject(str3, Float.class));
                        assertEquals(d3, JSON.parseObject(str3, Double.class));
                        assertEquals(f3, ((Float) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                        assertEquals(d3, ((Double) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                        for (int i4 = 0; i4 < 10; i4++) {
                            char c4 = (char) ('0' + i4);
                            chars[OFF + 6] = c4;

                            String str4 = new String(chars, 0, OFF + 7);
                            float f4 = Float.parseFloat(str4);
                            double d4 = Double.parseDouble(str4);
                            assertEquals(f4, JSON.parseObject(str4, Float.class));
                            assertEquals(d4, JSON.parseObject(str4, Double.class));
                            assertEquals(f4, ((Float) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                            assertEquals(d4, ((Double) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void test15() {
        char[] chars = new char[]{
                '0', '.',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
        };
        int OFF = 10;
        for (int i0 = 0; i0 < 2; i0++) {
            char c0 = (char) ('0' + i0);
            chars[OFF + 2] = c0;

            String str0 = new String(chars, 0, OFF + 3);
            float f0 = Float.parseFloat(str0);
            double d0 = Double.parseDouble(str0);
            assertEquals(f0, JSON.parseObject(str0, Float.class));
            assertEquals(d0, JSON.parseObject(str0, Double.class));
            assertEquals(f0, ((Float) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
            assertEquals(d0, ((Double) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[OFF + 3] = c1;

                String str1 = new String(chars, 0, OFF + 4);
                float f1 = Float.parseFloat(str1);
                double d1 = Double.parseDouble(str1);
                assertEquals(f1, JSON.parseObject(str1, Float.class));
                assertEquals(d1, JSON.parseObject(str1, Double.class));
                assertEquals(f1, ((Float) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                assertEquals(d1, ((Double) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[OFF + 4] = c2;

                    String str2 = new String(chars, 0, OFF + 5);
                    float f2 = Float.parseFloat(str2);
                    double d2 = Double.parseDouble(str2);
                    assertEquals(f2, JSON.parseObject(str2, Float.class));
                    assertEquals(d2, JSON.parseObject(str2, Double.class));
                    assertEquals(f2, ((Float) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                    assertEquals(d2, ((Double) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[OFF + 5] = c3;

                        String str3 = new String(chars, 0, OFF + 6);
                        float f3 = Float.parseFloat(str3);
                        double d3 = Double.parseDouble(str3);
                        assertEquals(f3, JSON.parseObject(str3, Float.class));
                        assertEquals(d3, JSON.parseObject(str3, Double.class));
                        assertEquals(f3, ((Float) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                        assertEquals(d3, ((Double) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                        for (int i4 = 0; i4 < 10; i4++) {
                            char c4 = (char) ('0' + i4);
                            chars[OFF + 6] = c4;

                            String str4 = new String(chars, 0, OFF + 7);
                            float f4 = Float.parseFloat(str4);
                            double d4 = Double.parseDouble(str4);
                            assertEquals(f4, JSON.parseObject(str4, Float.class));
                            assertEquals(d4, JSON.parseObject(str4, Double.class));
                            assertEquals(f4, ((Float) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                            assertEquals(d4, ((Double) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void test20() {
        char[] chars = new char[]{
                '0', '.',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
        };
        int OFF = 15;
        for (int i0 = 0; i0 < 2; i0++) {
            char c0 = (char) ('0' + i0);
            chars[OFF + 2] = c0;

            String str0 = new String(chars, 0, OFF + 3);
            float f0 = Float.parseFloat(str0);
            double d0 = Double.parseDouble(str0);
            assertEquals(f0, JSON.parseObject(str0, Float.class));
            assertEquals(d0, JSON.parseObject(str0, Double.class));
            assertEquals(f0, ((Float) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
            assertEquals(d0, ((Double) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[OFF + 3] = c1;

                String str1 = new String(chars, 0, OFF + 4);
                float f1 = Float.parseFloat(str1);
                double d1 = Double.parseDouble(str1);
                assertEquals(f1, JSON.parseObject(str1, Float.class));
                assertEquals(d1, JSON.parseObject(str1, Double.class));
                assertEquals(f1, ((Float) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                assertEquals(d1, ((Double) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[OFF + 4] = c2;

                    String str2 = new String(chars, 0, OFF + 5);
                    float f2 = Float.parseFloat(str2);
                    double d2 = Double.parseDouble(str2);
                    assertEquals(f2, JSON.parseObject(str2, Float.class));
                    assertEquals(d2, JSON.parseObject(str2, Double.class));
                    assertEquals(f2, ((Float) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                    assertEquals(d2, ((Double) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[OFF + 5] = c3;

                        String str3 = new String(chars, 0, OFF + 6);
                        float f3 = Float.parseFloat(str3);
                        double d3 = Double.parseDouble(str3);
                        assertEquals(f3, JSON.parseObject(str3, Float.class));
                        assertEquals(d3, JSON.parseObject(str3, Double.class));
                        assertEquals(f3, ((Float) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                        assertEquals(d3, ((Double) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                        for (int i4 = 0; i4 < 10; i4++) {
                            char c4 = (char) ('0' + i4);
                            chars[OFF + 6] = c4;

                            String str4 = new String(chars, 0, OFF + 7);
                            float f4 = Float.parseFloat(str4);
                            double d4 = Double.parseDouble(str4);
                            assertEquals(f4, JSON.parseObject(str4, Float.class));
                            assertEquals(d4, JSON.parseObject(str4, Double.class));
                            assertEquals(f4, ((Float) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                            assertEquals(d4, ((Double) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void test25() {
        char[] chars = new char[]{
                '0', '.',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
        };
        int OFF = 20;
        for (int i0 = 0; i0 < 2; i0++) {
            char c0 = (char) ('0' + i0);
            chars[OFF + 2] = c0;

            String str0 = new String(chars, 0, OFF + 3);
            float f0 = Float.parseFloat(str0);
            double d0 = Double.parseDouble(str0);
            assertEquals(f0, JSON.parseObject(str0, Float.class));
            assertEquals(d0, JSON.parseObject(str0, Double.class));
            assertEquals(f0, ((Float) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
            assertEquals(d0, ((Double) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[OFF + 3] = c1;

                String str1 = new String(chars, 0, OFF + 4);
                float f1 = Float.parseFloat(str1);
                double d1 = Double.parseDouble(str1);
                assertEquals(f1, JSON.parseObject(str1, Float.class));
                assertEquals(d1, JSON.parseObject(str1, Double.class));
                assertEquals(f1, ((Float) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                assertEquals(d1, ((Double) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[OFF + 4] = c2;

                    String str2 = new String(chars, 0, OFF + 5);
                    float f2 = Float.parseFloat(str2);
                    double d2 = Double.parseDouble(str2);
                    assertEquals(f2, JSON.parseObject(str2, Float.class));
                    assertEquals(d2, JSON.parseObject(str2, Double.class));
                    assertEquals(f2, ((Float) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                    assertEquals(d2, ((Double) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[OFF + 5] = c3;

                        String str3 = new String(chars, 0, OFF + 6);
                        float f3 = Float.parseFloat(str3);
                        double d3 = Double.parseDouble(str3);
                        assertEquals(f3, JSON.parseObject(str3, Float.class));
                        assertEquals(d3, JSON.parseObject(str3, Double.class));
                        assertEquals(f3, ((Float) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                        assertEquals(d3, ((Double) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                        for (int i4 = 0; i4 < 10; i4++) {
                            char c4 = (char) ('0' + i4);
                            chars[OFF + 6] = c4;

                            String str4 = new String(chars, 0, OFF + 7);
                            float f4 = Float.parseFloat(str4);
                            double d4 = Double.parseDouble(str4);
                            assertEquals(f4, JSON.parseObject(str4, Float.class));
                            assertEquals(d4, JSON.parseObject(str4, Double.class));
                            assertEquals(f4, ((Float) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                            assertEquals(d4, ((Double) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void test30() {
        char[] chars = new char[]{
                '0', '.',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
        };
        int OFF = 25;
        for (int i0 = 0; i0 < 2; i0++) {
            char c0 = (char) ('0' + i0);
            chars[OFF + 2] = c0;

            String str0 = new String(chars, 0, OFF + 3);
            float f0 = Float.parseFloat(str0);
            double d0 = Double.parseDouble(str0);
            assertEquals(f0, JSON.parseObject(str0, Float.class));
            assertEquals(d0, JSON.parseObject(str0, Double.class));
            assertEquals(f0, ((Float) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
            assertEquals(d0, ((Double) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[OFF + 3] = c1;

                String str1 = new String(chars, 0, OFF + 4);
                float f1 = Float.parseFloat(str1);
                double d1 = Double.parseDouble(str1);
                assertEquals(f1, JSON.parseObject(str1, Float.class));
                assertEquals(d1, JSON.parseObject(str1, Double.class));
                assertEquals(f1, ((Float) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                assertEquals(d1, ((Double) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[OFF + 4] = c2;

                    String str2 = new String(chars, 0, OFF + 5);
                    float f2 = Float.parseFloat(str2);
                    double d2 = Double.parseDouble(str2);
                    assertEquals(f2, JSON.parseObject(str2, Float.class));
                    assertEquals(d2, JSON.parseObject(str2, Double.class));
                    assertEquals(f2, ((Float) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                    assertEquals(d2, ((Double) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[OFF + 5] = c3;

                        String str3 = new String(chars, 0, OFF + 6);
                        float f3 = Float.parseFloat(str3);
                        double d3 = Double.parseDouble(str3);
                        assertEquals(f3, JSON.parseObject(str3, Float.class));
                        assertEquals(d3, JSON.parseObject(str3, Double.class));
                        assertEquals(f3, ((Float) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                        assertEquals(d3, ((Double) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                        for (int i4 = 0; i4 < 10; i4++) {
                            char c4 = (char) ('0' + i4);
                            chars[OFF + 6] = c4;

                            String str4 = new String(chars, 0, OFF + 7);
                            float f4 = Float.parseFloat(str4);
                            double d4 = Double.parseDouble(str4);
                            assertEquals(f4, JSON.parseObject(str4, Float.class));
                            assertEquals(d4, JSON.parseObject(str4, Double.class));
                            assertEquals(f4, ((Float) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                            assertEquals(d4, ((Double) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void test35() {
        char[] chars = new char[]{
                '0', '.',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
                '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
        };
        int OFF = 30;
        for (int i0 = 0; i0 < 2; i0++) {
            char c0 = (char) ('0' + i0);
            chars[OFF + 2] = c0;

            String str0 = new String(chars, 0, OFF + 3);
            float f0 = Float.parseFloat(str0);
            double d0 = Double.parseDouble(str0);
            assertEquals(f0, JSON.parseObject(str0, Float.class));
            assertEquals(d0, JSON.parseObject(str0, Double.class));
            assertEquals(f0, ((Float) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
            assertEquals(d0, ((Double) JSON.parse(str0, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

            for (int i1 = 0; i1 < 10; i1++) {
                char c1 = (char) ('0' + i1);
                chars[OFF + 3] = c1;

                String str1 = new String(chars, 0, OFF + 4);
                float f1 = Float.parseFloat(str1);
                double d1 = Double.parseDouble(str1);
                assertEquals(f1, JSON.parseObject(str1, Float.class));
                assertEquals(d1, JSON.parseObject(str1, Double.class));
                assertEquals(f1, ((Float) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                assertEquals(d1, ((Double) JSON.parse(str1, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                for (int i2 = 0; i2 < 10; i2++) {
                    char c2 = (char) ('0' + i2);
                    chars[OFF + 4] = c2;

                    String str2 = new String(chars, 0, OFF + 5);
                    float f2 = Float.parseFloat(str2);
                    double d2 = Double.parseDouble(str2);
                    assertEquals(f2, JSON.parseObject(str2, Float.class));
                    assertEquals(d2, JSON.parseObject(str2, Double.class));
                    assertEquals(f2, ((Float) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                    assertEquals(d2, ((Double) JSON.parse(str2, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                    for (int i3 = 0; i3 < 10; i3++) {
                        char c3 = (char) ('0' + i3);
                        chars[OFF + 5] = c3;

                        String str3 = new String(chars, 0, OFF + 6);
                        float f3 = Float.parseFloat(str3);
                        double d3 = Double.parseDouble(str3);
                        assertEquals(f3, JSON.parseObject(str3, Float.class));
                        assertEquals(d3, JSON.parseObject(str3, Double.class));
                        assertEquals(f3, ((Float) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                        assertEquals(d3, ((Double) JSON.parse(str3, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());

                        for (int i4 = 0; i4 < 10; i4++) {
                            char c4 = (char) ('0' + i4);
                            chars[OFF + 6] = c4;

                            String str4 = new String(chars, 0, OFF + 7);
                            float f4 = Float.parseFloat(str4);
                            double d4 = Double.parseDouble(str4);
                            assertEquals(f4, JSON.parseObject(str4, Float.class));
                            assertEquals(d4, JSON.parseObject(str4, Double.class));
                            assertEquals(f4, ((Float) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForFloats)).floatValue());
                            assertEquals(d4, ((Double) JSON.parse(str4, JSONReader.Feature.UseBigDecimalForDoubles)).doubleValue());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void test_x6() {
        char[] chars = new char[64];
        chars[0] = '0';
        chars[1] = '.';

        for (int i = 0; i < 1000_000; ++i) {
            int len = IOUtils.stringSize(i);
            IOUtils.getChars(i, len + 2, chars);
            JSONReader jsonReader = JSONReader.of(chars, 0, len + 2);

            String str = new String(chars, 0, len + 2);
            double doubleValue = Double.parseDouble(str);
            assertEquals(doubleValue, jsonReader.readDoubleValue());
        }
    }

    @Test
    public void test_x10() {
        int off = 4;

        char[] chars = new char[64];
        chars[0] = '0';
        chars[1] = '.';
        for (int i = 2; i < 2 + off; i++) {
            chars[i] = '0';
        }

        for (int i = 0; i < 1000_000; ++i) {
            int len = IOUtils.stringSize(i);
            IOUtils.getChars(i, len + off + 2, chars);
            JSONReader jsonReader = JSONReader.of(chars, 0, len + off + 2);

            String str = new String(chars, 0, len + off + 2);
            double doubleValue = Double.parseDouble(str);
            assertEquals(doubleValue, jsonReader.readDoubleValue());
        }
    }

    @Test
    public void test_x12() {
        int off = 6;

        char[] chars = new char[64];
        chars[0] = '0';
        chars[1] = '.';
        for (int i = 2; i < 2 + off; i++) {
            chars[i] = '0';
        }

        for (int i = 0; i < 1000_000; ++i) {
            int len = IOUtils.stringSize(i);
            IOUtils.getChars(i, len + off + 2, chars);
            JSONReader jsonReader = JSONReader.of(chars, 0, len + off + 2);

            String str = new String(chars, 0, len + off + 2);
            double doubleValue = Double.parseDouble(str);
            assertEquals(doubleValue, jsonReader.readDoubleValue());
        }
    }

    @Test
    public void test_x15() {
        int off = 9;

        char[] chars = new char[64];
        chars[0] = '0';
        chars[1] = '.';
        for (int i = 2; i < 2 + off; i++) {
            chars[i] = '0';
        }

        for (int i = 0; i < 1000_000; ++i) {
            int len = IOUtils.stringSize(i);
            IOUtils.getChars(i, len + off + 2, chars);
            JSONReader jsonReader = JSONReader.of(chars, 0, len + off + 2);

            String str = new String(chars, 0, len + off + 2);
            double doubleValue = Double.parseDouble(str);
            assertEquals(doubleValue, jsonReader.readDoubleValue());
        }
    }

    @Test
    public void test_x16() {
        int off = 10;

        char[] chars = new char[64];
        chars[0] = '0';
        chars[1] = '.';
        for (int i = 2; i < 2 + off; i++) {
            chars[i] = '0';
        }

        for (int i = 0; i < 1000_000; ++i) {
            int len = IOUtils.stringSize(i);
            IOUtils.getChars(i, len + off + 2, chars);
            JSONReader jsonReader = JSONReader.of(chars, 0, len + off + 2);

            String str = new String(chars, 0, len + off + 2);
            double doubleValue = Double.parseDouble(str);
            assertEquals(doubleValue, jsonReader.readDoubleValue());
        }
    }

    @Test
    public void test_bug_0() {
        {
            String str = "[-122.422003528252475, 37.808480096967251,-122.42082593937107, 37.808631474146033,-122.420825939371]";
            JSONArray array = JSON.parseArray(str, JSONReader.Feature.UseBigDecimalForDoubles);
            assertEquals(-122.422003528252475D, array.getDouble(0));
            assertEquals(37.808480096967251D, array.getDouble(1));
            assertEquals(-122.42082593937107D, array.getDouble(2));
            assertEquals(37.808631474146033D, array.getDouble(3));
            assertEquals(-122.420825939371D, array.getDouble(4));
        }
        {
            String str = "[-122.422003528252475, 37.808480096967251,-122.42082593937107, 37.808631474146033,-122.420825939371]";
            JSONArray array = JSON.parseArray(str, JSONReader.Feature.UseBigDecimalForFloats);
            assertEquals(-122.422003528252475F, array.getDouble(0));
            assertEquals(37.808480096967251F, array.getDouble(1));
            assertEquals(-122.42082593937107F, array.getDouble(2));
            assertEquals(37.808631474146033F, array.getDouble(3));
            assertEquals(-122.420825939371F, array.getDouble(4));
        }
    }
}
