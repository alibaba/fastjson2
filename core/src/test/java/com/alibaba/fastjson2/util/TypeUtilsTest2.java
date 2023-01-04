package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TypeUtilsTest2 {
    @Test
    public void parseInt() {
        assertNull(TypeUtils.parseInt(new byte[0], 0, 0));
        assertNull(TypeUtils.parseBigDecimal(new byte[0], 0, 0));

        int[] values = new int[]{
                1, 2, 3, 4, 5,
                11, 12, 13, 14, 15,
                101, 102, 103, 104, 105,
                1001, 1002, 1003, 1004, 1005,
                10001, 10002, 10003, 10004, 10005,
                100001, 100002, 100003, 100004, 100005,
                1000001, 1000002, 1000003, 1000004, 1000005,
                10000001, 10000002, 10000003, 10000004, 10000005,
                100000001, 100000002, 100000003, 100000004, 100000005,
                1000000001, 1000000002, 1000000003, 1000000004, 1000000005,
                Integer.MIN_VALUE, Integer.MAX_VALUE, 0
        };

        for (int value : values) {
            String str = Integer.toString(value);
            byte[] bytes = str.getBytes();
            assertEquals(value, TypeUtils.parseInt(bytes, 0, bytes.length));
        }

        for (int value : values) {
            if (value > 0) {
                value = -value;
            }
            String str = Integer.toString(value);
            byte[] bytes = str.getBytes();
            assertEquals(value, TypeUtils.parseInt(bytes, 0, bytes.length));
        }

        for (int value : values) {
            String str = Integer.toString(value);
            byte[] bytes = str.getBytes();
            assertEquals(BigDecimal.valueOf(value), TypeUtils.parseBigDecimal(bytes, 0, bytes.length));
        }
    }

    @Test
    public void parseIntRandom() {
        Random r = new Random();

        for (int i = 0; i < 1000; i++) {
            int value = r.nextInt();
            String str = Integer.toString(value);
            byte[] bytes = str.getBytes();
            assertEquals(value, TypeUtils.parseInt(bytes, 0, bytes.length));
        }

        for (int i = 0; i < 1000; i++) {
            long value = r.nextLong();
            String str = Long.toString(value);
            byte[] bytes = str.getBytes();
            assertEquals(value, TypeUtils.parseLong(bytes, 0, bytes.length));
        }
    }

    @Test
    public void parseLong() {
        assertNull(TypeUtils.parseLong(new byte[0], 0, 0));

        long[] values = new long[]{
                1, 2, 3, 4, 5,
                11, 12, 13, 14, 15,
                101, 102, 103, 104, 105,
                1001, 1002, 1003, 1004, 1005,
                10001, 10002, 10003, 10004, 10005,
                100001, 100002, 100003, 100004, 100005,
                1000001, 1000002, 1000003, 1000004, 1000005,
                10000001, 10000002, 10000003, 10000004, 10000005,
                100000001, 100000002, 100000003, 100000004, 100000005,
                1000000001, 1000000002, 1000000003, 1000000004, 1000000005,
                10000000001L, 10000000002L, 10000000003L, 10000000004L, 10000000005L,
                100000000001L, 100000000002L, 100000000003L, 100000000004L, 100000000005L,
                1000000000001L, 1000000000002L, 1000000000003L, 1000000000004L, 1000000000005L,
                10000000000001L, 10000000000002L, 10000000000003L, 10000000000004L, 10000000000005L,
                100000000000001L, 100000000000002L, 100000000000003L, 100000000000004L, 100000000000005L,
                1000000000000001L, 1000000000000002L, 1000000000000003L, 1000000000000004L, 1000000000000005L,
                10000000000000001L, 10000000000000002L, 10000000000000003L, 10000000000000004L, 10000000000000005L,
                100000000000000001L, 100000000000000002L, 100000000000000003L, 100000000000000004L, 100000000000000005L,
                1000000000000000001L, 1000000000000000002L, 1000000000000000003L, 1000000000000000004L, 1000000000000000005L,
                Long.MIN_VALUE, Long.MAX_VALUE, 0
        };

        for (long value : values) {
            if (value > 0) {
                value = -value;
            }

            String str = Long.toString(value);
            byte[] bytes = str.getBytes();
            assertEquals(value, TypeUtils.parseLong(bytes, 0, bytes.length));
        }
    }

    @Test
    public void parseBoolean() {
        assertNull(TypeUtils.parseBoolean(new byte[0], 0, 0));

        String[] trueStrings = new String[]{
                "1", "Y", "true"
        };

        for (String str : trueStrings) {
            byte[] bytes = str.getBytes();
            assertTrue(TypeUtils.parseBoolean(bytes, 0, bytes.length));
        }

        String[] falseStrings = new String[]{
                "0", "N", "false"
        };

        for (String str : falseStrings) {
            byte[] bytes = str.getBytes();
            assertFalse(TypeUtils.parseBoolean(bytes, 0, bytes.length));
        }

        String[] errorString = new String[]{
                "X", "XX", "XXX", "XXXX", "XXXXX", "XXXXXX"
        };
        for (String str : errorString) {
            byte[] bytes = str.getBytes();
            assertEquals(Boolean.parseBoolean(str), TypeUtils.parseBoolean(bytes, 0, bytes.length));
        }
    }
}
