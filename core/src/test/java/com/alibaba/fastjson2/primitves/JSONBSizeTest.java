package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSONBSizeTest {
    @Test
    public void test_byte() throws Exception {
        byte[] values_1 = new byte[]{
                -1, -8, -16, -32, -64, -128,
                0, 1, 2, 4, 8, 16, 32, 48, 96, 127
        };
        for (int i = 0; i < values_1.length; i++) {
            Byte val = values_1[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 2, bytes.length);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals("input " + val, val, JSONB.parseObject(bytes, Number.class));

            assertEquals(Short.valueOf(val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf(val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));
        }
    }

    @Test
    public void test_short() throws Exception {
        short[] values_1 = new short[]{
                -1, -8, -16, -32, -64, -128, -256, -512, -1024, -2048, -4096, -9182, -16384, -32768,
                0, 1, 2, 4, 8, 16, 32, 48, 96, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32767
        };
        for (int i = 0; i < values_1.length; i++) {
            Short val = values_1[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 3, bytes.length);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals("input " + val, val, JSONB.parseObject(bytes, Number.class));

            assertEquals(Byte.valueOf(val.byteValue()), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf(val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf(val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));
        }
    }

    @Test
    public void test_int() throws Exception {
        int[] values_1 = new int[]{
                -1, -8, -16, 0, 1, 2, 4, 8, 16, 32, 47
        };
        for (int i = 0; i < values_1.length; i++) {
            int val = values_1[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 1, bytes.length);
            assertEquals(val, ((Integer) JSONB.parse(bytes)).intValue());
            assertEquals("input " + val, val, JSONB.parseObject(bytes, Number.class));

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf(val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Integer.toString(val), JSONB.parseObject(bytes, String.class));
        }

        int[] values_ = new int[]{
                -17, -32, -64, -128, -256, -512, -1024, -2048,
                48, 96, 128, 256, 512, 1024, 2047
        };
        for (int i = 0; i < values_.length; i++) {
            int val = values_[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 2, bytes.length);
            assertEquals(val, ((Integer) JSONB.parse(bytes)).intValue());
            assertEquals("input " + val, val, JSONB.parseObject(bytes, Number.class));

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf(val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Integer.toString(val), JSONB.parseObject(bytes, String.class));
        }

        int[] values_3 = new int[]{
                -2049, -4096, -8192, -16384, -32768, -65536, -131072, -262144,
                2048, 4096, 8192, 16384, 32768, 65536, 131072, 262143
        };
        for (int i = 0; i < values_3.length; i++) {
            int val = values_3[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 3, bytes.length);
            assertEquals(val, ((Integer) JSONB.parse(bytes)).intValue());
            assertEquals("input " + val, val, JSONB.parseObject(bytes, Number.class));

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf(val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Integer.toString(val), JSONB.parseObject(bytes, String.class));
        }

        int[] values_5 = new int[]{
                -262145, Integer.MIN_VALUE, 262144, Integer.MAX_VALUE
        };
        for (int i = 0; i < values_5.length; i++) {
            int val = values_5[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 5, bytes.length);
            assertEquals(val, ((Integer) JSONB.parse(bytes)).intValue());
            assertEquals("input " + val, val, JSONB.parseObject(bytes, Number.class));

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf(val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Integer.toString(val), JSONB.parseObject(bytes, String.class));
        }
    }

    @Test
    public void test_long() throws Exception {
        long[] values_1 = new long[]{
                -1, -8, 0, 1, 2, 4, 8, 15
        };
        for (int i = 0; i < values_1.length; i++) {
            long val = values_1[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 1, bytes.length);
            assertEquals(val, ((Long) JSONB.parse(bytes)).longValue());
            assertTrue(val == JSONB.parseObject(bytes, Number.class).longValue());
            assertEquals("input " + val, val, JSONB.parseObject(bytes, Number.class).longValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Long.toString(val), JSONB.parseObject(bytes, String.class));
        }

        long[] values_2 = new long[]{
                -9, -17, -32, -64, -128, -256, -512, -1024, -2048,
                16, 24, 32, 48, 96, 128, 256, 512, 1024, 2047
        };
        for (int i = 0; i < values_2.length; i++) {
            long val = values_2[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 2, bytes.length);
            assertEquals(val, ((Long) JSONB.parse(bytes)).longValue());
            assertEquals("input " + val, val, JSONB.parseObject(bytes, Number.class).longValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Long.toString(val), JSONB.parseObject(bytes, String.class));
        }

        long[] values_3 = new long[]{
                -2049, -4096, -8192, -16384, -32768, -65536, -131072, -262144,
                2048, 3858, 8192, 16384, 32768, 65536, 131072, 262143
        };
        for (int i = 0; i < values_3.length; i++) {
            long val = values_3[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 3, bytes.length);
            assertEquals(val, ((Long) JSONB.parse(bytes)).longValue());
            assertTrue(val == JSONB.parseObject(bytes, Number.class).longValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Long.toString(val), JSONB.parseObject(bytes, String.class));
        }

        long[] values_5 = new long[]{
                -262145, Integer.MIN_VALUE, 262144, Integer.MAX_VALUE
        };
        for (int i = 0; i < values_5.length; i++) {
            long val = values_5[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 5, bytes.length);
            assertEquals(val, ((Long) JSONB.parse(bytes)).longValue());
            assertTrue(val == JSONB.parseObject(bytes, Number.class).longValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Long.toString(val), JSONB.parseObject(bytes, String.class));
        }

        long[] values_9 = new long[]{
                Integer.MIN_VALUE - 1L, Long.MIN_VALUE, Integer.MAX_VALUE + 1L, Long.MAX_VALUE
        };
        for (int i = 0; i < values_9.length; i++) {
            long val = values_9[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 9, bytes.length);
            assertEquals(val, ((Long) JSONB.parse(bytes)).longValue());
            assertTrue(val == JSONB.parseObject(bytes, Number.class).longValue());

            assertEquals("input " + val, Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals("input " + val, Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals("input " + val, Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals("input " + val, Long.valueOf(val), JSONB.parseObject(bytes, Long.class));
            assertEquals("input " + val, Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals("input " + val, Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals("input " + val, BigInteger.valueOf(val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals("input " + val, BigDecimal.valueOf(val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Long.toString(val), JSONB.parseObject(bytes, String.class));
        }
    }

    @Test
    public void test_float() throws Exception {
        float[] values_2 = new float[]{
                -1, -8, -16, 0, 1, 2, 4, 8, 16, 32, 47
        };
        for (int i = 0; i < values_2.length; i++) {
            float val = values_2[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 2, bytes.length);
            assertTrue(val == ((Float) JSONB.parse(bytes)).floatValue());
            assertTrue(val == JSONB.parseObject(bytes, Number.class).floatValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Float.toString(val), JSONB.parseObject(bytes, String.class));
        }

        float[] values_3 = new float[]{
                -17, -32, -64, -128, -256, -512, -1024, -2048,
                48, 96, 128, 256, 512, 1024, 2047
        };
        for (int i = 0; i < values_3.length; i++) {
            float val = values_3[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 3, bytes.length);
            assertTrue(val == ((Float) JSONB.parse(bytes)).floatValue());
            assertTrue(val == JSONB.parseObject(bytes, Number.class).floatValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Float.toString(val), JSONB.parseObject(bytes, String.class));
        }


        float[] values_4 = new float[]{
                -2049, -4096, -8192, -16384, -32768, -65536, -131072, -262144,
                2048, 4096, 8192, 16384, 32768, 65536, 131072, 262143
        };
        for (int i = 0; i < values_4.length; i++) {
            float val = values_4[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 4, bytes.length);
            assertTrue(val == ((Float) JSONB.parse(bytes)).floatValue());
            assertTrue(val == JSONB.parseObject(bytes, Number.class).floatValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Float.toString(val), JSONB.parseObject(bytes, String.class));
        }

        float[] values_5 = new float[]{
                -262145, Integer.MIN_VALUE, 262144, Integer.MAX_VALUE, -262145.1f, 262144.1f, Float.MIN_VALUE, Float.MAX_VALUE, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY
        };
        for (int i = 0; i < values_5.length; i++) {
            float val = values_5[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 5, bytes.length);
            float parsedValue = ((Float) JSONB.parse(bytes)).floatValue();
            if (Float.isNaN(val)) {
                assertTrue(Float.isNaN(parsedValue));
            } else {
                assertTrue(val == parsedValue);
                assertTrue(val == ((Float) JSONB.parseObject(bytes, Number.class)).floatValue());
            }

            assertEquals("input " + val, Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals("input " + val, Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf(val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Float.toString(val), JSONB.parseObject(bytes, String.class));
        }
    }

    @Test
    public void test_double() throws Exception {
        double[] values_1 = new double[]{
                0, 1
        };
        for (int i = 0; i < values_1.length; i++) {
            double val = values_1[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 1, bytes.length);
            assertTrue(val == ((Double) JSONB.parse(bytes)).doubleValue());
            assertTrue(val == ((Double) JSONB.parseObject(bytes, Number.class)).doubleValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf((float) val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Double.toString(val), JSONB.parseObject(bytes, String.class));
        }

        double[] values_2 = new double[]{
                -1, -2, -4, -8, 2, 3, 4, 8, 15
        };
        for (int i = 0; i < values_2.length; i++) {
            double val = values_2[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 2, bytes.length);
            assertTrue(val == ((Double) JSONB.parse(bytes)).doubleValue());
            assertTrue(val == ((Double) JSONB.parseObject(bytes, Number.class)).doubleValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf((float) val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Double.toString(val), JSONB.parseObject(bytes, String.class));
        }

        double[] values_3 = new double[]{
                -9, -16, -32, -64, -128, -256, -512, -1024, -2048,
                16, 32, 48, 96, 128, 256, 512, 1024, 2047
        };
        for (int i = 0; i < values_3.length; i++) {
            double val = values_3[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 3, bytes.length);
            assertTrue(val == ((Double) JSONB.parse(bytes)).doubleValue());
            assertTrue(val == ((Double) JSONB.parseObject(bytes, Number.class)).doubleValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf((float) val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Double.toString(val), JSONB.parseObject(bytes, String.class));
        }

        double[] values_4 = new double[]{
                -2049, -4096, -8192, -16384, -32768, -65536, -131072, -262144,
                2048, 3858, 8192, 16384, 32768, 65536, 131072, 262143
        };
        for (int i = 0; i < values_4.length; i++) {
            double val = values_4[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 4, bytes.length);
            assertTrue(val == ((Double) JSONB.parse(bytes)).doubleValue());
            assertTrue(val == ((Double) JSONB.parseObject(bytes, Number.class)).doubleValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf((float) val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Double.toString(val), JSONB.parseObject(bytes, String.class));
        }


        double[] values_6 = new double[]{
                -262145, Integer.MIN_VALUE, 262144, Integer.MAX_VALUE
        };
        for (int i = 0; i < values_6.length; i++) {
            double val = values_6[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 6, bytes.length);
            assertTrue(val == ((Double) JSONB.parse(bytes)).doubleValue());
            assertTrue(val == ((Double) JSONB.parseObject(bytes, Number.class)).doubleValue());

            assertEquals(Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals(Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf((float) val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Double.toString(val), JSONB.parseObject(bytes, String.class));
        }

        double[] values_9 = new double[]{
                Integer.MIN_VALUE -1L, Integer.MAX_VALUE + 1L, Long.MIN_VALUE, Long.MAX_VALUE
                -262145.1d, 262144.1d
                , Float.MIN_VALUE, Float.MAX_VALUE, Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY
                , Double.MIN_VALUE, Double.MAX_VALUE, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
        };
        for (int i = 0; i < values_9.length; i++) {
            double val = values_9[i];
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, 9, bytes.length);
            double parsedValue = ((Double) JSONB.parse(bytes)).doubleValue();
            if (Double.isNaN(val)) {
                assertTrue(Double.isNaN(parsedValue));
            } else {
                assertTrue(val == parsedValue);
                assertTrue(val == ((Double) JSONB.parseObject(bytes, Number.class)).doubleValue());
            }

            assertEquals("input " + val, Byte.valueOf((byte) val), JSONB.parseObject(bytes, Byte.class));
            assertEquals("input " + val, Short.valueOf((short) val), JSONB.parseObject(bytes, Short.class));
            assertEquals(Integer.valueOf((int) val), JSONB.parseObject(bytes, Integer.class));
            assertEquals(Long.valueOf((long) val), JSONB.parseObject(bytes, Long.class));
            assertEquals(Float.valueOf((float) val), JSONB.parseObject(bytes, Float.class));
            assertEquals(Double.valueOf(val), JSONB.parseObject(bytes, Double.class));
            assertEquals(BigInteger.valueOf((long) val), JSONB.parseObject(bytes, BigInteger.class));
            assertEquals(BigDecimal.valueOf((long) val), JSONB.parseObject(bytes, BigDecimal.class));

            assertEquals(Double.toString(val), JSONB.parseObject(bytes, String.class));
        }
    }

    @Test
    public void test_str() throws Exception {
        char[] chars = new char[1024 * 1024];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('0' + (i % 9));
        }

        for (int i = 0; i < 48; ++i) {
            String val = new String(chars, 0, i);
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, val.length() + 1, bytes.length);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
        }

        for (int i = 48; i < 2048; ++i) {
            String val = new String(chars, 0, i);
            byte[] bytes = JSONB.toBytes(val);
            assertEquals("input " + val, val.length() + 3, bytes.length);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
        }

        {
            String val = new String(chars, 0, 2048);
            byte[] bytes = JSONB.toBytes(val);
            assertEquals(val.length() + 4, bytes.length);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
        }
        {
            String val = new String(chars, 0, 262143);
            byte[] bytes = JSONB.toBytes(val);
            assertEquals(val.length() + 4, bytes.length);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
        }

        {
            String val = new String(chars, 0, 262144);
            byte[] bytes = JSONB.toBytes(val);
            assertEquals(val.length() + 6, bytes.length);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
        }
        {
            String val = new String(chars);
            byte[] bytes = JSONB.toBytes(val);
            assertEquals(val.length() + 6, bytes.length);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
        }
    }

    @Test
    public void test_str_utf8() throws Exception {
        char[] chars = new char[1024];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('０' + (i % 9));
            String val = new String(chars, 0, i);
            byte[] bytes = JSONB.toBytes(val);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
        }
    }

    @Test
    public void test_str_utf16() throws Exception {
        char[] chars = new char[1024];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('０' + (i % 9));
            String val = new String(chars, 0, i);
            byte[] bytes = JSONB.toBytes(val, StandardCharsets.UTF_16);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
            assertEquals(JSON.toJSONString(val), JSONB.toJSONString(bytes));
        }
    }

    @Test
    public void test_str_utf16be() throws Exception {
        char[] chars = new char[1024];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('０' + (i % 9));
            String val = new String(chars, 0, i);
            byte[] bytes = JSONB.toBytes(val, StandardCharsets.UTF_16BE);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
            assertEquals(JSON.toJSONString(val), JSONB.toJSONString(bytes));
        }
    }

    @Test
    public void test_str_utf16le() throws Exception {
        char[] chars = new char[1024];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('０' + (i % 9));
            String val = new String(chars, 0, i);
            byte[] bytes = JSONB.toBytes(val, StandardCharsets.UTF_16LE);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
            assertEquals(JSON.toJSONString(val), JSONB.toJSONString(bytes));
        }
    }

    @Test
    public void test_str_GB18030() throws Exception {
        char[] chars = new char[1024];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('０' + (i % 9));
            String val = new String(chars, 0, i);
            byte[] bytes = JSONB.toBytes(val, IOUtils.GB18030);
            assertEquals(val, JSONB.parse(bytes));
            assertEquals(val, JSONB.parseObject(bytes, String.class));
            assertEquals(val, JSONB.parseObject(bytes, CharSequence.class));
            assertEquals(JSON.toJSONString(val), JSONB.toJSONString(bytes));
        }
    }
}
