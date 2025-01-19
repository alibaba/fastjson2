package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static com.alibaba.fastjson2.util.JDKUtils.*;
import static org.junit.jupiter.api.Assertions.*;

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
        int[] values = new int[]{
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
        long[] values = new long[]{
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
    public void digit3_chars() {
        assertEquals(197,
                IOUtils.digit3(
                        "1972".toCharArray(), 0));

        char[] chars = new char[4];
        for (int x0 = -1; x0 <= 10; x0++) {
            chars[0] = (char) (x0 + '0');
            for (int x1 = -1; x1 <= 10; x1++) {
                chars[1] = (char) (x1 + '0');
                for (int x2 = -1; x2 <= 10; x2++) {
                    chars[2] = (char) (x2 + '0');
                    int d3 = IOUtils.digit3(chars, 0);
                    int expect;
                    if (x0 < 0 || x0 > 9 || x1 < 0 || x1 > 9 || x2 < 0 || x2 > 9) {
                        expect = -1;
                    } else {
                        expect = x0 * 100 + x1 * 10 + x2;
                    }
                    assertEquals(expect, d3);
                }
            }
        }
    }

    @Test
    public void digit3() {
        assertEquals(197,
                IOUtils.digit3(
                        "1972".getBytes(StandardCharsets.UTF_8), 0));

        byte[] bytes = new byte[4];
        for (int x0 = -1; x0 <= 10; x0++) {
            bytes[0] = (byte) (x0 + '0');
            for (int x1 = -1; x1 <= 10; x1++) {
                bytes[1] = (byte) (x1 + '0');
                for (int x2 = -1; x2 <= 10; x2++) {
                    bytes[2] = (byte) (x2 + '0');
                    int d3 = IOUtils.digit3(bytes, 0);
                    int expect;
                    if (x0 < 0 || x0 > 9 || x1 < 0 || x1 > 9 || x2 < 0 || x2 > 9) {
                        expect = -1;
                    } else {
                        expect = x0 * 100 + x1 * 10 + x2;
                    }
                    assertEquals(expect, d3);
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

    @Test
    public void printDigit() {
        System.out.println("0xF " + Integer.toBinaryString(0xF));
        System.out.println("0xF0 " + Integer.toBinaryString(0xF0));
        System.out.println("0x60" + Integer.toBinaryString(0x60));
        System.out.println();

        for (int i = 0; i < 10; i++) {
            char c = (char) ('0' + i);
            System.out.println(c + " " + Integer.toBinaryString(c)
                    + " & " + Integer.toBinaryString(0xF0)
                    + " = " + Integer.toBinaryString(c & 0xF0));
        }

        System.out.println();
        for (int i = 0; i < 10; i++) {
            char c = (char) ('0' + i);
            System.out.println(c + " ((" + Integer.toBinaryString(c)
                    + ") & " + Integer.toBinaryString(0xF)
                    + " + " + Integer.toBinaryString(0x60)
                    + ") & " + Integer.toBinaryString(0xF0)
                    + " = " + Integer.toBinaryString(((c & 0xF) + 0x60) & 0xF0));
        }
    }

    @Test
    public void printDigitHex() {
        char[] chars = "0123456789abcdefABCDEF".toCharArray();
        System.out.println("0xF " + Integer.toBinaryString(0xF));
        System.out.println("0xF0 " + Integer.toBinaryString(0xF0));
        System.out.println("0x60" + Integer.toBinaryString(0x60));
        System.out.println();

        long X = 0xA_000A_0000_0000L;
        for (char c : chars) {
            System.out.println(c + "\t" + ((int) c)
                    + "\t" + Long.toBinaryString(c)
                    + "\t" + Long.toHexString((c & 0xF0) >> 4)
                    + "\t" + Long.toHexString(c & 0xF)
                    + "\t" + Long.toHexString(((c & 0xF0) >> 1))
                    + "\t" + Long.toHexString(((X >> ((c & 0xF0) >> 1))) & 0xFF)
                    + "\t" + Long.toHexString(((c & 0xF) + ((X >> ((c & 0xF0) >> 1))) & 0xFF))
            );
        }
    }

    @Test
    public void hexDigit4() {
        byte[] bytes = "1234ABcd".getBytes(StandardCharsets.UTF_8);
        assertEquals("1234", Integer.toHexString(IOUtils.hexDigit4(bytes, 0)));
        assertEquals("34ab", Integer.toHexString(IOUtils.hexDigit4(bytes, 2)));

        char[] chars = "1234ABcd".toCharArray();
        assertEquals("1234", Integer.toHexString(IOUtils.hexDigit4(chars, 0)));
        assertEquals("34ab", Integer.toHexString(IOUtils.hexDigit4(chars, 2)));
    }

    static int hexDigit4(byte[] bytes, int offset) {
        long v = Long.reverseBytes(UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset));
        v = (v & 0x0F0F0F0F_0F0F0F0FL) + ((((v & 0x40404040_40404040L) >> 2) | ((v & 0x40404040_40404040L) << 1)) >>> 4);
        v = ((v >>> 28) & 0xF0000000L)
                + ((v >>> 24) & 0xF000000)
                + ((v >>> 20) & 0xF00000)
                + ((v >>> 16) & 0xF0000)
                + ((v >>> 12) & 0xF000)
                + ((v >>> 8) & 0xF00)
                + ((v >>> 4) & 0xF0)
                + (v & 0xF);
        return (int) v;
    }

    @Test
    public void indexOf() {
        byte[] bytes = "'b'd'".getBytes(StandardCharsets.UTF_8);
        assertEquals(2,
                IOUtils.indexOfQuote(
                        bytes, '\'', 1, bytes.length));
        assertEquals(0,
                IOUtils.indexOfQuote(
                        bytes, '\'', 0, bytes.length));
        assertEquals(4,
                IOUtils.indexOfQuote(
                        bytes, '\'', 3, bytes.length));
    }

    @Test
    public void indexOf1() {
        byte[] bytes = "\"b\"d\"".getBytes(StandardCharsets.UTF_8);
        assertEquals(2,
                IOUtils.indexOfQuote(
                        bytes, '"', 1, bytes.length));
        assertEquals(0,
                IOUtils.indexOfQuote(
                        bytes, '"', 0, bytes.length));
        assertEquals(4,
                IOUtils.indexOfQuote(
                        bytes, '"', 3, bytes.length));
    }

    @Test
    public void indexOfSlash() {
        byte[] bytes = "\\b\\d\\".getBytes(StandardCharsets.UTF_8);
        assertEquals(2,
                IOUtils.indexOfSlash(
                        bytes, 1, bytes.length));
        assertEquals(0,
                IOUtils.indexOfSlash(
                        bytes, 0, bytes.length));
        assertEquals(4,
                IOUtils.indexOfSlash(
                        bytes, 3, bytes.length));
    }

    @Test
    public void convEndian() throws Throwable {
        Random r = new Random();
        long i64 = r.nextLong();
        int i32 = r.nextInt();
        short i16 = (short) r.nextInt();

        assertEquals(i64, IOUtils.convEndian(false, i64));
        assertEquals(Long.reverseBytes(i64), IOUtils.convEndian(true, i64));

        assertEquals(i32, IOUtils.convEndian(false, i32));
        assertEquals(Integer.reverseBytes(i32), IOUtils.convEndian(true, i32));

        assertEquals(i16, IOUtils.convEndian(false, i16));
        assertEquals(Short.reverseBytes(i16), IOUtils.convEndian(true, i16));
    }

    @Test
    public void test_isASCII() {
        char[] chars = new char[] {'0', '1', '2', '3', '4', '5', '6', 0x80};
        long v = UNSAFE.getLong(chars, ARRAY_CHAR_BASE_OFFSET);
        assertTrue(IOUtils.isASCII(chars, 0, 4));
        assertTrue(IOUtils.isASCII(chars, 4, 4));
    }
}
