package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TypeUtilsTest2 {
    @Test
    public void parseInt() {
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

            char[] chars = str.toCharArray();
            assertEquals(value, TypeUtils.parseInt(chars, 0, bytes.length));
        }

        for (int value : values) {
            if (value > 0) {
                value = -value;
            }
            String str = Integer.toString(value);
            byte[] bytes = str.getBytes();
            assertEquals(value, TypeUtils.parseInt(bytes, 0, bytes.length));

            char[] chars = str.toCharArray();
            assertEquals(value, TypeUtils.parseInt(chars, 0, bytes.length));
        }

        for (int value : values) {
            String str = Integer.toString(value);
            byte[] bytes = str.getBytes();
            BigDecimal expected = BigDecimal.valueOf(value);
            assertEquals(expected, TypeUtils.parseBigDecimal(bytes, 0, bytes.length));

            char[] chars = str.toCharArray();
            assertEquals(expected, TypeUtils.parseBigDecimal(chars, 0, bytes.length));
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

            char[] chars = str.toCharArray();
            assertEquals(value, TypeUtils.parseInt(chars, 0, bytes.length));
        }

        for (int i = 0; i < 1000; i++) {
            long value = r.nextLong();
            String str = Long.toString(value);
            byte[] bytes = str.getBytes();
            assertEquals(value, TypeUtils.parseLong(bytes, 0, bytes.length));
            char[] chars = str.toCharArray();
            assertEquals(value, TypeUtils.parseLong(chars, 0, bytes.length));
        }
    }

    @Test
    public void parseLong() {
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

            char[] chars = str.toCharArray();
            assertEquals(value, TypeUtils.parseLong(chars, 0, bytes.length));
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

    @Test
    public void parseBigDecimal() {
        String[] strings = new String[]{
                "12.34",
                ".34",
                "123.",
                "123.4",
                "1234.56",
                "1.23456E1",
                "123E1",
                "123E-1",
                "1234567890.1234567890",
                "12345678901234567890.1234567890",
                "123456789012345678901234567890.1234567890",
                "1234567890123456789012345678901234567890.1234567890",
                "12345678901234567890123456789012345678901234567890.1234567890",
                "123456789012345678901234567890123456789012345678901234567890.1234567890",
                "1234567890123456789012345678901234567890123456789012345678901234567890.1234567890",
                "-12.34",
                "-.34",
                "-123.",
                "-123.4",
                "-1234.56",
                "-1.23456",
                "-1.23456E1",
                "-123E1",
                "-123E-1",
                "-1234567890.1234567890",
                "-12345678901234567890.1234567890",
                "-123456789012345678901234567890.1234567890",
                "-1234567890123456789012345678901234567890.1234567890",
                "-12345678901234567890123456789012345678901234567890.1234567890",
                "-123456789012345678901234567890123456789012345678901234567890.1234567890",
                "-1234567890123456789012345678901234567890123456789012345678901234567890.1234567890"
        };

        for (String string : strings) {
            byte[] bytes = ("a," + string).getBytes();
            BigDecimal decimal = TypeUtils.parseBigDecimal(bytes, 2, string.length());
            assertEquals(new BigDecimal(string), decimal);

            char[] chars = ("a," + string).toCharArray();
            BigDecimal decimal1 = TypeUtils.parseBigDecimal(chars, 2, string.length());
            assertEquals(new BigDecimal(string), decimal1);
        }

        for (String string : strings) {
            byte[] bytes = ("ab," + string).getBytes();
            BigDecimal decimal = TypeUtils.parseBigDecimal(bytes, 3, string.length());
            assertEquals(new BigDecimal(string), decimal);

            char[] chars = ("ab," + string).toCharArray();
            BigDecimal decimal1 = TypeUtils.parseBigDecimal(chars, 3, string.length());
            assertEquals(new BigDecimal(string), decimal1);
        }

        for (String string : strings) {
            byte[] bytes = ("abc," + string).getBytes();
            BigDecimal decimal = TypeUtils.parseBigDecimal(bytes, 4, string.length());
            assertEquals(new BigDecimal(string), decimal);

            char[] chars = ("abc," + string).toCharArray();
            BigDecimal decimal1 = TypeUtils.parseBigDecimal(chars, 4, string.length());
            assertEquals(new BigDecimal(string), decimal1);
        }

        assertNull(TypeUtils.parseBigDecimal(new byte[128], 0, 0));
        assertNull(TypeUtils.parseBigDecimal(new char[128], 0, 0));
    }

    @Test
    public void isInteger() {
        String[] strings = new String[]{
                "+",
                "-",
                "A",
                "+A",
                "+1A",
                "+1A",
                "123.",
                "+123.",
                "-123.",
                ".123",
                "+.123",
                "-.123"
        };
        for (String string : strings) {
            assertFalse(TypeUtils.isInteger(string));

            byte[] bytes = string.getBytes();
            assertFalse(TypeUtils.isInteger(bytes, 0, bytes.length));

            String prefix = "ABC";
            byte[] bytes2 = (prefix + string).getBytes();
            assertFalse(TypeUtils.isInteger(bytes2, prefix.length(), string.length()));
        }

        String[] trues = new String[]{
                "+1",
                "-1",
                "1",
                "123",
                "+123",
                "-123",
        };
        for (String string : trues) {
            assertTrue(TypeUtils.isInteger(string));

            byte[] bytes = string.getBytes();
            assertTrue(TypeUtils.isInteger(bytes, 0, bytes.length));

            String prefix = "ABC";
            byte[] bytes2 = (prefix + string).getBytes();
            assertTrue(TypeUtils.isInteger(bytes2, prefix.length(), string.length()));
        }
    }

    @Test
    public void isNumber1() {
        String[] strings = new String[]{
                "+",
                "-",
                "A",
                "+A",
                "+1A",
                "+1A",
                ".",
                ".12.",
                ".12A."
        };
        for (String string : strings) {
            assertFalse(TypeUtils.isNumber(string));

            byte[] bytes = string.getBytes();
            assertFalse(TypeUtils.isNumber(bytes, 0, bytes.length));

            String prefix = "ABC";
            byte[] bytes2 = (prefix + string).getBytes();
            assertFalse(TypeUtils.isNumber(bytes2, prefix.length(), string.length()));
        }

        String[] trues = new String[]{
                "+1",
                "-1",
                "1",
                "123",
                "+123",
                "-123",
                "123.",
                "+123.",
                "-123.",
                ".123",
                ".123E",
                ".123e",
        };
        for (String string : trues) {
            assertTrue(TypeUtils.isNumber(string));

            byte[] bytes = string.getBytes();
            assertTrue(TypeUtils.isNumber(bytes, 0, bytes.length));

            String prefix = "ABC";
            byte[] bytes2 = (prefix + string).getBytes();
            assertTrue(
                    TypeUtils.isNumber(bytes2, prefix.length(), string.length()), string
            );
        }
    }
}
