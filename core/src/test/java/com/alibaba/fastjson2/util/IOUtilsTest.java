package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(
                JSONException.class,
                () -> IOUtils.encodeUTF8(bytes, 0, bytes.length, dst, 0));
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
    public void digit4() {
        byte[] bytes = new byte[4];
        for (int x0 = -1; x0 <= 10; x0++) {
            bytes[0] = (byte) (x0 + '0');
            for (int x1 = -1; x1 <= 10; x1++) {
                bytes[1] = (byte) (x1 + '0');
                for (int x2 = -1; x2 <= 10; x2++) {
                    bytes[2] = (byte) (x2 + '0');
                    for (int x3 = -1; x3 <= 10; x3++) {
                        bytes[3] = (byte) (x3 + '0');
                        int d4 = IOUtils.digit4(bytes, 0);
                        int expect;
                        if (x0 < 0 || x0 > 9 || x1 < 0 || x1 > 9 || x2 < 0 || x2 > 9 || x3 < 0 || x3 > 9) {
                            expect = -1;
                        } else {
                            expect = x0 * 1000 + x1 * 100 + x2 * 10 + x3;
                        }
                        assertEquals(expect, d4);
                    }
                }
            }
        }
    }

    @Test
    public void digit4_chars() {
        assertEquals(1972,
                IOUtils.digit4(
                        "1972".toCharArray(), 0));

        char[] chars = new char[4];
        for (int x0 = -1; x0 <= 10; x0++) {
            chars[0] = (char) (x0 + '0');
            for (int x1 = -1; x1 <= 10; x1++) {
                chars[1] = (char) (x1 + '0');
                for (int x2 = -1; x2 <= 10; x2++) {
                    chars[2] = (char) (x2 + '0');
                    for (int x3 = -1; x3 <= 10; x3++) {
                        chars[3] = (char) (x3 + '0');
                        int d4 = IOUtils.digit4(chars, 0);
                        int expect;
                        if (x0 < 0 || x0 > 9 || x1 < 0 || x1 > 9 || x2 < 0 || x2 > 9 || x3 < 0 || x3 > 9) {
                            expect = -1;
                        } else {
                            expect = x0 * 1000 + x1 * 100 + x2 * 10 + x3;
                        }
                        assertEquals(expect, d4);
                    }
                }
            }
        }
    }

    @Test
    public void digit2() {
        byte[] bytes = new byte[2];
        for (int x0 = -1; x0 <= 10; x0++) {
            bytes[0] = (byte) (x0 + '0');
            for (int x1 = -1; x1 <= 10; x1++) {
                bytes[1] = (byte) (x1 + '0');
                int d2 = IOUtils.digit2(bytes, 0);
                int expect;
                if (x0 < 0 || x0 > 9 || x1 < 0 || x1 > 9) {
                    expect = -1;
                } else {
                    expect = x0 * 10 + x1;
                }
                assertEquals(expect, d2);
            }
        }
    }

    @Test
    public void digit2_chars() {
        assertEquals(19,
                IOUtils.digit2(
                        "19".toCharArray(), 0));

        char[] bytes = new char[2];
        for (int x0 = -1; x0 <= 10; x0++) {
            bytes[0] = (char) (x0 + '0');
            for (int x1 = -1; x1 <= 10; x1++) {
                bytes[1] = (char) (x1 + '0');
                int d2 = IOUtils.digit2(bytes, 0);
                int expect;
                if (x0 < 0 || x0 > 9 || x1 < 0 || x1 > 9) {
                    expect = -1;
                } else {
                    expect = x0 * 10 + x1;
                }
                assertEquals(expect, d2);
            }
        }
    }
}
