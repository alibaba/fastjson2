package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IOUtilsTest {
    @Test
    public void size() {
        assertEquals(Long.toString(Long.MAX_VALUE), toString(Long.MAX_VALUE));
        assertEquals(Long.toString(Long.MAX_VALUE), toString1(Long.MAX_VALUE));
    }

    static String toString(long i) {
        byte[] bytes = new byte[20];
        int size = IOUtils.stringSize(i);
        IOUtils.getChars(i, size, bytes);
        return new String(bytes, 0, size);
    }

    static String toString1(long i) {
        char[] chars = new char[20];
        int size = IOUtils.stringSize(i);
        IOUtils.getChars(i, size, chars);
        return new String(chars, 0, size);
    }

    @Test
    public void encodeUTF8() {
        String str = "×中\uD83D\uDC81\uD83D\uDC4C\uD83C\uDF8D\uD83D\uDE0D";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_16LE);

        byte[] dst = new byte[100];
        int result = IOUtils.encodeUTF8(bytes, 0, bytes.length, dst, 0);
        String str2 = new String(dst, 0, result, StandardCharsets.UTF_8);
        assertEquals(str, str2);
    }

    @Test
    public void encodeUTF8_error() {
        byte[] bytes = new byte[]{-127, -36};
        byte[] dst = new byte[100];
        int result = IOUtils.encodeUTF8(bytes, 0, bytes.length, dst, 0);
        assertEquals(-1, result);
    }

    @Test
    public void getChars() {
        int[] values = new int[] {
                1,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000,
                Integer.MAX_VALUE
        };

        for (int k = 0; k < values.length; k++) {
            int i = values[k];
            String str = Integer.toString(i);

            int size = IOUtils.stringSize(i);
            byte[] bytes = new byte[size];
            IOUtils.getChars(i, size, bytes);
            assertEquals(str, new String(bytes));

            char[] chars = new char[size];
            IOUtils.getChars(i, size, chars);
            assertEquals(str, new String(chars));

            int n = -i;
            String str_n = Integer.toString(n);

            int size_n = size + 1;
            byte[] bytes_n = new byte[size_n];
            IOUtils.getChars(n, size_n, bytes_n);
            assertEquals(str_n, new String(bytes_n));

            char[] chars_n = new char[size_n];
            IOUtils.getChars(n, size_n, chars_n);
            assertEquals(str_n, new String(chars_n));
        }
    }

    @Test
    public void getCharsLong() {
        long[] values = new long[] {
                1,
                10,
                100,
                1000,
                10000,
                100000,
                1000000,
                10000000,
                100000000,
                1000000000,
                10000000000L,
                100000000000L,
                1000000000000L,
                10000000000000L,
                100000000000000L,
                1000000000000000L,
                10000000000000000L,
                100000000000000000L,
                1000000000000000000L,
                Long.MAX_VALUE
        };

        for (int k = 0; k < values.length; k++) {
            long i = values[k];
            String str = Long.toString(i);

            int size = IOUtils.stringSize(i);
            byte[] bytes = new byte[size];
            IOUtils.getChars(i, size, bytes);
            assertEquals(str, new String(bytes));

            char[] chars = new char[size];
            IOUtils.getChars(i, size, chars);
            assertEquals(str, new String(chars));

            long n = -i;
            String str_n = Long.toString(n);

            int size_n = size + 1;
            byte[] bytes_n = new byte[size_n];
            IOUtils.getChars(n, size_n, bytes_n);
            assertEquals(str_n, new String(bytes_n));

            char[] chars_n = new char[size_n];
            IOUtils.getChars(n, size_n, chars_n);
            assertEquals(str_n, new String(chars_n));
        }
    }
}
