package com.alibaba.fastjson2.primitves;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArrayNumberTest {
    @Test
    public void test_array() {
        Object[] array = new Object[]{
                (Byte.valueOf((byte) 0)),
                Short.valueOf((short) 0),
                Integer.valueOf(0),
                Long.valueOf(0),
                BigInteger.valueOf(0),
                BigDecimal.valueOf(0),
        };
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,0,0,0,0,0]", str);

        assertEquals(Arrays.toString(array),
                Arrays.toString((Object[])
                        JSONReader.of(str)
                                .read(array.getClass())
                )
        );
    }

    @Test
    public void test_array_1() {
        Number[] array = new Number[]{
                (Byte.valueOf((byte) 0)),
                Short.valueOf((short) 0),
                Integer.valueOf(0),
                Long.valueOf(0),
                BigInteger.valueOf(0),
                BigDecimal.valueOf(0),
        };
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,0,0,0,0,0]", str);

        assertEquals(Arrays.toString(array),
                Arrays.toString((Object[])
                        JSONReader.of(str)
                                .read(array.getClass())
                )
        );
    }

    @Test
    public void test_array_Byte() {
        Byte[] array = new Byte[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_byte() {
        byte[] array = new byte[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_Short() {
        Short[] array = new Short[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_short() {
        short[] array = new short[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_integer() {
        Integer[] array = new Integer[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_int() {
        int[] array = new int[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_int_dim2() {
        int[][] array = new int[][]{{0, 1, 2, 3, 4}, {5, 6}, {7, 8}};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[[0,1,2,3,4],[5,6],[7,8]]", str);

        int[][] array2 = JSONReader.of(str).read(array.getClass());
        assertEquals(array.length, array2.length);
        for (int i = 0; i < array.length; ++i) {
            assertTrue(
                    Arrays.equals(array[i], array2[i]));
        }
    }

    @Test
    public void test_array_Long() {
        Long[] array = new Long[]{0L, 1L, 2L, 3L, 4L};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_long() {
        long[] array = new long[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_BigInt() {
        BigInteger[] array = new BigInteger[]{BigInteger.valueOf(0),
                BigInteger.valueOf(1),
                BigInteger.valueOf(2),
                BigInteger.valueOf(3),
                BigInteger.valueOf(4)
        };
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_BigDecimal() {
        BigDecimal[] array = new BigDecimal[]{BigDecimal.valueOf(0),
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(4)
        };
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0,1,2,3,4]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_Float() {
        Float[] array = new Float[]{0F, 1F, 2F, 3F, 4F};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0.0,1.0,2.0,3.0,4.0]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_float() {
        float[] array = new float[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0.0,1.0,2.0,3.0,4.0]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_Double() {
        Double[] array = new Double[]{0D, 1D, 2D, 3D, 4D};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0.0,1.0,2.0,3.0,4.0]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }

    @Test
    public void test_array_double() {
        double[] array = new double[]{0, 1, 2, 3, 4};
        JSONWriter jw = JSONWriter.of();
        jw.writeAny(array);
        String str = jw.toString();
        assertEquals("[0.0,1.0,2.0,3.0,4.0]", str);

        assertTrue(Arrays.equals(array,
                JSONReader.of(str)
                        .read(array.getClass())));
    }
}
