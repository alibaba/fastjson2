package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import org.apache.arrow.flatbuf.Int;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_BYTE_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;
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
        System.out.println(Integer.toHexString(IOUtils.hexDigit4(bytes, 0)));
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
    public void indexOf() throws Throwable {
        byte[] bytes = "abcda".getBytes(StandardCharsets.UTF_8);
        assertEquals(2,
                IOUtils.indexOfChar(
                        bytes, 'c', 0));
        assertEquals(0,
                IOUtils.indexOfChar(
                        bytes, 'a', 0));
        assertEquals(4,
                IOUtils.indexOfChar(
                        bytes, 'a', 1));
    }

    @Test
    public void x0() {
        for (int i = 0; i < 256; i++) {
            byte b = (byte) i;
            if (b < ' ') {
                System.out.println(i + "\t" + Integer.toHexString(i));
            }
        }
    }

    @Test
    public void xor() {
        for (int i = 0; i < 256; i++) {
            byte b = (byte) i;
//            if ((b ^ 0x22) != 0 && (b ^ 0x27) != 0 && (b ^ 0x5c) != 0) {
//                continue;
//            }
//            if (((i & 0x80) != 0)) {
//                continue;
//            }
////
//            System.out.println(i + "\t" + Integer.toHexString(i) + "\t" + Integer.toHexString((0x70 - i + 0x30) & 0x80) + "\t" + (char) i);
//
//            if (contains(i, 0x22)) {
//                System.out.println(i + "\t" + Integer.toHexString(i) + "\t" + (char) i);
//            }
//            if ((b ^ 0x22) == 0) {
//                System.out.println(i + "\t" + Integer.toHexString(i) + "\t" + (char) i);
//                continue;
//            }
            if (isSpecial(i)) {
                continue;
            }

            System.out.println(i + "\t" + Integer.toHexString(i) + "\t" + (char) i);
        }
    }

    @Test
    public void xor_0() {
        for (int i = 0; i < 256; i++) {
            if (((i & 0x80) != 0)) {
                continue;
            }

            int x = i >> 4;

            System.out.println(i + "\t" + Integer.toHexString(x) + "\t" + Integer.toHexString((((0x7 - x + 0x3) & 0x8))) + "\t" + (char) i);
        }
    }

    public static boolean isSpecial(long data) {
        long xed = data ^ 0x2222_2222_2222_2222L;
        long xf0 = data ^ 0x5c5c5c5c5c5c5c5cL;

        xed = (xed - 0x0101010101010101L) & ~xed;
        xf0 = (xf0 - 0x0101010101010101L) & ~xf0;

        return ((xed | xf0 | (0x7F7F_7F7F_7F7F_7F7FL - data + 0x1010_1010_1010_1010L) | data) & 0x8080808080808080L) != 0;
    }

    public static boolean isSpecial1(long data) {
        // 在Java中，long是有符号的，所以我们需要确保我们正确处理高字节。
        long xed = data ^ 0xededededededededL;
        long xf0 = (data | 0x1010101010101010L) ^ 0xf0f0f0f0f0f0f0f0L;
        long xf4 = data ^ 0xf4f4f4f4f4f4f4f4L;

        // Java没有&^运算符，所以用~和&来模拟
        xed = (xed - 0x0101010101010101L) & ~xed;
        xf0 = (xf0 - 0x0101010101010101L) & ~xf0;
        xf4 = (xf4 - 0x0101010101010101L) & ~xf4;

        return ((xed | xf0 | xf4) & 0x8080808080808080L) != 0;
    }


    public static boolean contains(long data, long mask) {
        // Perform XOR on data and mask
        data ^= mask;

        // Subtract the magic number from data
        // Use a mask to emulate unsigned behavior for the highest bit
        long magic = 0x0101010101010101L;
        long highBitMask = 0x8080808080808080L;
        long result = (data - magic) & ~data & highBitMask;

        // Return true if any byte in the result is non-zero
        return result != 0;
    }



    @Test
    public void xor_1() {
        long MASK = 0x2222_2222_2222_2222L;
        long[] values = {
                0x1522_1314_1522_1314L,
                0x1534_1534_1534_1534L,
                MASK,
         };
        for (long i : values) {
            System.out.println(Long.toHexString(i) + "\t" + isSpecial(i));
        }
    }

    @Test
    public void x6() {
        for (int i = 0; i < 6; i++) {
            int x = i << 4;
            System.out.println(i + "\t" + x + "\t" + (x ^ 0x70) + "\t" + (((x ^ 0x70) + 0x20) & 0x80));
        }
    }

    @Test
    public void hex() {
        for (int i = 0; i < 16; i++) {
            System.out.println(i + "\t" + Integer.toHexString(i) + "\t" + Integer.toBinaryString(i));
        }
    }
}
