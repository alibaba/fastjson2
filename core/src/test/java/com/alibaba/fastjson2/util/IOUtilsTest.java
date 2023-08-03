package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

import static com.alibaba.fastjson2.util.IOUtils.DIGITS_K;
import static com.alibaba.fastjson2.util.IOUtils.DIGITS_K_64;
import static com.alibaba.fastjson2.util.JDKUtils.*;
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

    @Test
    public void getCharsLong1() {
        long[] values = new long[] {
                1,
                10,
                12,
                100,
                123,
                1000,
                1234,
                10000,
                12345,
                100000,
                123456,
                1000000,
                1234567,
                10000000,
                12345678,
                100000000,
                123456789,
                1000000000,
                1234567891,
                10000000000L,
                12345678912L,
                100000000000L,
                123456789123L,
                1000000000000L,
                1234567891234L,
                10000000000000L,
                12345678912345L,
                100000000000000L,
                123456789123456L,
                1000000000000000L,
                1234567891234567L,
                10000000000000000L,
                12345678912345678L,
                100000000000000000L,
                123456789123456789L,
                1000000000000000000L,
                1234567891234567891L,
                -1,
                -10,
                -12,
                -100,
                -123,
                -1000,
                -1234,
                -10000,
                -12345,
                -100000,
                -123456,
                -1000000,
                -1234567,
                -10000000,
                -12345678,
                -100000000,
                -123456789,
                -1000000000,
                -1234567891,
                -10000000000L,
                -12345678912L,
                -100000000000L,
                -123456789123L,
                -1000000000000L,
                -1234567891234L,
                -10000000000000L,
                -12345678912345L,
                -100000000000000L,
                -123456789123456L,
                -1000000000000000L,
                -1234567891234567L,
                -10000000000000000L,
                -12345678912345678L,
                -100000000000000000L,
                -123456789123456789L,
                -1000000000000000000L,
                -1234567891234567891L,
                Long.MAX_VALUE,
                Long.MIN_VALUE
        };

        for (int i = 0; i < values.length; i++) {
            long d = values[i];
            String str = Long.toString(d);

            byte[] bytes = new byte[20];
            int size = IOUtils.writeInt64(bytes, 0, d);
            assertEquals(str.length(), size, str);
            assertEquals(str, new String(bytes, 0, size), str);

            char[] chars = new char[20];
            int size1 = IOUtils.writeInt64(chars, 0, d);
            assertEquals(str.length(), size1, str);
            assertEquals(str, new String(chars, 0, size1), str);
        }
    }

    @Test
    public void getCharsInt1() {
        int[] values = new int[] {
                1,
                10,
                12,
                100,
                123,
                1000,
                1234,
                10000,
                12345,
                100000,
                123456,
                1000000,
                1234567,
                10000000,
                12345678,
                100000000,
                123456789,
                1000000000,
                1234567891,
                -1,
                -10,
                -12,
                -100,
                -123,
                -1000,
                -1234,
                -10000,
                -12345,
                -100000,
                -123456,
                -1000000,
                -1234567,
                -10000000,
                -12345678,
                -100000000,
                -123456789,
                -1000000000,
                -1234567891,
                Integer.MAX_VALUE,
                Integer.MIN_VALUE
        };

        for (int i = 0; i < values.length; i++) {
            int d = values[i];
            String str = Integer.toString(d);

            byte[] bytes = new byte[20];
            int size = IOUtils.writeInt32(bytes, 0, d);
            assertEquals(str.length(), size, str);

            assertEquals(str.length(), size);
            assertEquals(str, new String(bytes, 0, size), str);

            char[] chars = new char[20];
            int size1 = IOUtils.writeInt32(chars, 0, d);
            assertEquals(str.length(), size1, str);
            assertEquals(str, new String(chars, 0, size1), str);
        }
    }

    @Test
    public void digitK() {
        int[] digits = DIGITS_K;

        byte[] bytes = new byte[20];
        for (int i = 0; i < digits.length; i++) {
            String str = Integer.toString(i);
            int d = digits[i];

            int size = (byte) d;
            assertEquals(str.length(), size);

            UNSAFE.putInt(bytes, ARRAY_BYTE_BASE_OFFSET, d >> ((4 - size) << 3));
            String str1 = new String(bytes, 0, size);
            assertEquals(str, str1);
        }
    }

    @Test
    public void digitK64() {
        long[] digits = DIGITS_K_64;

        char[] bytes = new char[20];
        for (int i = 0; i < digits.length; i++) {
            String str = Integer.toString(i);
            long d = digits[i];

            int size = (byte) d;
            assertEquals(str.length(), size);

            UNSAFE.putLong(bytes, ARRAY_CHAR_BASE_OFFSET, d >> ((4 - size) << 4));
            String str1 = new String(bytes, 0, size);
            assertEquals(str, str1);
        }
    }

    @Test
    public void writeLocalDate() {
        char[] chars = new char[20];
        int size = IOUtils.writeLocalDate(chars, 0, 2013, 12, 30);
        assertEquals("2013-12-30", new String(chars, 0, size));
    }

    @Test
    public void writeLocalTime() {
        char[] chars = new char[20];
        int size = IOUtils.writeLocalTime(chars, 0, LocalTime.of(12, 13, 14));
        assertEquals("12:13:14", new String(chars, 0, size));
    }

    @Test
    public void writeLocalTime1() {
        char[] chars = new char[18];
        int size = IOUtils.writeLocalTime(chars, 0, LocalTime.of(12, 13, 14, 123456789));
        assertEquals("12:13:14.123456789", new String(chars, 0, size));
    }
}
