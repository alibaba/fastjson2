package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue296 {
    @Test
    public void test() {
        List<String> list = JSON.parseArray("[1]").toList(String.class);
        assertEquals(1, list.size());
        assertEquals("1", list.get(0));

        String[] values = JSONObject.of("values", JSONArray.of(1, 2L)).getObject("values", String[].class);
        assertEquals(2, values.length);
        assertEquals("1", values[0]);
        assertEquals("2", values[1]);

        int[] array = JSONArray.of("1", "2").to(int[].class);
        assertEquals(2, array.length);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);

        float[] floatArray = JSONArray.of("1", 2, 3L, BigDecimal.valueOf(4)).to(float[].class);
        assertEquals(4, floatArray.length);
        assertEquals(1, floatArray[0]);
        assertEquals(2, floatArray[1]);
        assertEquals(3, floatArray[2]);
        assertEquals(4, floatArray[3]);

        double[] doubles = JSONArray.of("1", 2, 3L, BigDecimal.valueOf(4)).to(double[].class);
        assertEquals(4, doubles.length);
        assertEquals(1, doubles[0]);
        assertEquals(2, doubles[1]);
        assertEquals(3, doubles[2]);
        assertEquals(4, doubles[3]);

        Float[] floats = JSONArray.of("1", 2, 3L, BigDecimal.valueOf(4)).to(Float[].class);
        assertEquals(4, floats.length);
        assertEquals(1, floats[0]);
        assertEquals(2, floats[1]);
        assertEquals(3, floats[2]);
        assertEquals(4, floats[3]);
    }

    @Test
    public void test1() {
        JSONArray array = JSON.parseArray("[[1],[2.0],[3],['4']]");
        {
            int[][] ints = array.to(int[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            byte[][] ints = array.to(byte[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            short[][] ints = array.to(short[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            long[][] ints = array.to(long[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            float[][] numbers = array.to(float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0]);
            assertEquals(2, numbers[1][0]);
            assertEquals(3, numbers[2][0]);
            assertEquals(4, numbers[3][0]);
        }
        {
            double[][] numbers = array.to(double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0]);
            assertEquals(2, numbers[1][0]);
            assertEquals(3, numbers[2][0]);
            assertEquals(4, numbers[3][0]);
        }
        {
            Byte[][] numbers = array.to(Byte[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Short[][] numbers = array.to(Short[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Integer[][] numbers = array.to(Integer[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Long[][] numbers = array.to(Long[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Float[][] numbers = array.to(Float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Double[][] numbers = array.to(Double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            BigDecimal[][] numbers = array.to(BigDecimal[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            BigInteger[][] numbers = array.to(BigInteger[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Number[][] numbers = array.to(Number[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
    }

    @Test
    public void test2() {
        JSONArray array = JSONArray.of(
                JSONArray.of(true),
                JSONArray.of(BigInteger.valueOf(2)),
                JSONArray.of(new AtomicInteger(3)),
                JSONArray.of(new AtomicLong(4))
        );

        {
            int[][] ints = array.to(int[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            byte[][] ints = array.to(byte[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            short[][] ints = array.to(short[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            long[][] ints = array.to(long[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            float[][] numbers = array.to(float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0]);
            assertEquals(2, numbers[1][0]);
            assertEquals(3, numbers[2][0]);
            assertEquals(4, numbers[3][0]);
        }
        {
            double[][] numbers = array.to(double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0]);
            assertEquals(2, numbers[1][0]);
            assertEquals(3, numbers[2][0]);
            assertEquals(4, numbers[3][0]);
        }
        {
            Byte[][] numbers = array.to(Byte[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Short[][] numbers = array.to(Short[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Integer[][] numbers = array.to(Integer[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Long[][] numbers = array.to(Long[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Float[][] numbers = array.to(Float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Double[][] numbers = array.to(Double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            BigDecimal[][] numbers = array.to(BigDecimal[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            BigInteger[][] numbers = array.to(BigInteger[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Number[][] numbers = array.to(Number[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
    }

    @Test
    public void test3() {
        JSONArray array = JSONArray.of(
                JSONArray.of((Object) null),
                JSONArray.of("null"),
                JSONArray.of(""),
                new String[]{""}
        );

        {
            int[][] ints = array.to(int[][].class);
            assertEquals(4, ints.length);
            assertEquals(0, ints[0][0]);
            assertEquals(0, ints[1][0]);
            assertEquals(0, ints[2][0]);
            assertEquals(0, ints[3][0]);
        }
        {
            byte[][] ints = array.to(byte[][].class);
            assertEquals(4, ints.length);
            assertEquals(0, ints[0][0]);
            assertEquals(0, ints[1][0]);
            assertEquals(0, ints[2][0]);
            assertEquals(0, ints[3][0]);
        }
        {
            short[][] ints = array.to(short[][].class);
            assertEquals(4, ints.length);
            assertEquals(0, ints[0][0]);
            assertEquals(0, ints[1][0]);
            assertEquals(0, ints[2][0]);
            assertEquals(0, ints[3][0]);
        }
        {
            long[][] ints = array.to(long[][].class);
            assertEquals(4, ints.length);
            assertEquals(0, ints[0][0]);
            assertEquals(0, ints[1][0]);
            assertEquals(0, ints[2][0]);
            assertEquals(0, ints[3][0]);
        }
        {
            float[][] numbers = array.to(float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(0, numbers[0][0]);
            assertEquals(0, numbers[1][0]);
            assertEquals(0, numbers[2][0]);
            assertEquals(0, numbers[3][0]);
        }
        {
            double[][] numbers = array.to(double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(0, numbers[0][0]);
            assertEquals(0, numbers[1][0]);
            assertEquals(0, numbers[2][0]);
            assertEquals(0, numbers[3][0]);
        }
        {
            Byte[][] numbers = array.to(Byte[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
        {
            Short[][] numbers = array.to(Short[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
        {
            Integer[][] numbers = array.to(Integer[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
        {
            Long[][] numbers = array.to(Long[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
        {
            Float[][] numbers = array.to(Float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
        {
            Double[][] numbers = array.to(Double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
        {
            BigDecimal[][] numbers = array.to(BigDecimal[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
        {
            BigInteger[][] numbers = array.to(BigInteger[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
        {
            Number[][] numbers = array.to(Number[][].class);
            assertEquals(4, numbers.length);
            assertEquals(null, numbers[0][0]);
            assertEquals(null, numbers[1][0]);
            assertEquals(null, numbers[2][0]);
            assertEquals(null, numbers[3][0]);
        }
    }

    @Test
    public void test4() {
        JSONArray array = JSONArray.of(
                new boolean[]{true},
                new byte[]{2},
                new short[]{3},
                new int[]{4}
        );

        {
            int[][] ints = array.to(int[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            byte[][] ints = array.to(byte[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            short[][] ints = array.to(short[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            long[][] ints = array.to(long[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            float[][] numbers = array.to(float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0]);
            assertEquals(2, numbers[1][0]);
            assertEquals(3, numbers[2][0]);
            assertEquals(4, numbers[3][0]);
        }
        {
            double[][] numbers = array.to(double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0]);
            assertEquals(2, numbers[1][0]);
            assertEquals(3, numbers[2][0]);
            assertEquals(4, numbers[3][0]);
        }
        {
            Byte[][] numbers = array.to(Byte[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Short[][] numbers = array.to(Short[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Integer[][] numbers = array.to(Integer[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Long[][] numbers = array.to(Long[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Float[][] numbers = array.to(Float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Double[][] numbers = array.to(Double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            BigDecimal[][] numbers = array.to(BigDecimal[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            BigInteger[][] numbers = array.to(BigInteger[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Number[][] numbers = array.to(Number[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
    }

    @Test
    public void test5() {
        JSONArray array = JSONArray.of(
                new long[]{1},
                new float[]{2F},
                new double[]{3},
                new BigDecimal[]{BigDecimal.valueOf(4)}
        );

        {
            int[][] ints = array.to(int[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            byte[][] ints = array.to(byte[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            short[][] ints = array.to(short[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            long[][] ints = array.to(long[][].class);
            assertEquals(4, ints.length);
            assertEquals(1, ints[0][0]);
            assertEquals(2, ints[1][0]);
            assertEquals(3, ints[2][0]);
            assertEquals(4, ints[3][0]);
        }
        {
            float[][] numbers = array.to(float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0]);
            assertEquals(2, numbers[1][0]);
            assertEquals(3, numbers[2][0]);
            assertEquals(4, numbers[3][0]);
        }
        {
            double[][] numbers = array.to(double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0]);
            assertEquals(2, numbers[1][0]);
            assertEquals(3, numbers[2][0]);
            assertEquals(4, numbers[3][0]);
        }
        {
            Byte[][] numbers = array.to(Byte[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Short[][] numbers = array.to(Short[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Integer[][] numbers = array.to(Integer[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Long[][] numbers = array.to(Long[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Float[][] numbers = array.to(Float[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Double[][] numbers = array.to(Double[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            BigDecimal[][] numbers = array.to(BigDecimal[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            BigInteger[][] numbers = array.to(BigInteger[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
        {
            Number[][] numbers = array.to(Number[][].class);
            assertEquals(4, numbers.length);
            assertEquals(1, numbers[0][0].intValue());
            assertEquals(2, numbers[1][0].intValue());
            assertEquals(3, numbers[2][0].intValue());
            assertEquals(4, numbers[3][0].intValue());
        }
    }
}
