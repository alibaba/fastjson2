package com.alibaba.fastjson2.internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class ByteArrayTest {
    static ByteArray[] primitiveFunctionFactories() {
        return new ByteArray[] {
                new ByteArray(),
                new ByteArrayUnsafe(),
                new ByteArrayV()
        };
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void shortBytes(ByteArray byteArray) {
        byte[] buf = new byte[10];
        short value = (short) 0x1234;
        for (int i = 0; i < 4; i++) {
            byteArray.putShortBE(buf, i, value);
            assertEquals(value, byteArray.getShortBE(buf, i));

            byteArray.putShortLE(buf, i, value);
            assertEquals(value, byteArray.getShortLE(buf, i));

            byteArray.putShortUnaligned(buf, i, value);
            assertEquals(value, byteArray.getShortUnaligned(buf, i));
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void intBytes(ByteArray byteArray) {
        byte[] buf = new byte[10];
        int value = 0x12345678;
        for (int i = 0; i < 4; i++) {
            byteArray.putIntBE(buf, i, value);
            assertEquals(value, byteArray.getIntBE(buf, i));

            byteArray.putIntLE(buf, i, value);
            assertEquals(value, byteArray.getIntLE(buf, i));

            byteArray.putIntUnaligned(buf, i, value);
            assertEquals(value, byteArray.getIntUnaligned(buf, i));
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void intChars(ByteArray byteArray) {
        char[] src = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] dst = new char[src.length];
        for (int i = 0; i < 10; i++) {
            int value = byteArray.getIntBE(src, i);

            byteArray.putIntBE(dst, i, value);
            assertEquals(value, byteArray.getIntBE(dst, i));

            byteArray.putIntLE(dst, i, value);
            assertEquals(value, byteArray.getIntLE(dst, i));

            byteArray.putIntUnaligned(dst, i, value);
            assertEquals(value, byteArray.getIntUnaligned(dst, i));
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void longBytes(ByteArray byteArray) {
        byte[] buf = new byte[20];
        long value = 0x1234567890ABCDEFL;
        for (int i = 0; i < 10; i++) {
            byteArray.putLongBE(buf, i, value);
            assertEquals(value, byteArray.getLongBE(buf, i));

            byteArray.putLongLE(buf, i, value);
            assertEquals(value, byteArray.getLongLE(buf, i));

            byteArray.putLongUnaligned(buf, i, value);
            assertEquals(value, byteArray.getLongUnaligned(buf, i));
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void byteOperations(ByteArray byteArray) {
        byte[] buf = new byte[10];
        byte value = (byte) 0x7F;
        for (int i = 0; i < 10; i++) {
            byteArray.putByte(buf, i, value);
            assertEquals(value, byteArray.getByte(buf, i));
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void charOperations(ByteArray byteArray) {
        char[] buf = new char[10];
        char value = 'X';
        for (int i = 0; i < 10; i++) {
            byteArray.putChar(buf, i, value);
            assertEquals(value, byteArray.getChar(buf, i));
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void getCharFromBytes(ByteArray byteArray) {
        byte[] buf = new byte[10];
        // Test with a specific value
        buf[0] = (byte) ('A' & 0xFF);
        buf[1] = (byte) (('A' >> 8) & 0xFF);
        assertEquals('A', byteArray.getChar(buf, 0));

        // Test with another value
        buf[2] = (byte) ('Z' & 0xFF);
        buf[3] = (byte) (('Z' >> 8) & 0xFF);
        assertEquals('Z', byteArray.getChar(buf, 1));
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void longChars(ByteArray byteArray) {
        char[] src = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
        char[] dst = new char[src.length];
        for (int i = 0; i <= 16; i++) {
            long value = byteArray.getLongBE(src, i);

            byteArray.putLongBE(dst, i, value);
            assertEquals(value, byteArray.getLongBE(dst, i));

            byteArray.putLongLE(dst, i, value);
            assertEquals(value, byteArray.getLongLE(dst, i));

            byteArray.putLongUnaligned(dst, i, value);
            assertEquals(value, byteArray.getLongUnaligned(dst, i));
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void intCharsAdditional(ByteArray byteArray) {
        char[] src = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] dst = new char[src.length];
        for (int i = 0; i < 10; i++) {
            int value = byteArray.getIntLE(src, i);

            byteArray.putIntBE(dst, i, value);
            assertEquals(value, byteArray.getIntBE(dst, i));

            byteArray.putIntLE(dst, i, value);
            assertEquals(value, byteArray.getIntLE(dst, i));

            byteArray.putIntUnaligned(dst, i, value);
            assertEquals(value, byteArray.getIntUnaligned(dst, i));
        }
    }

    @ParameterizedTest
    @MethodSource("primitiveFunctionFactories")
    public void digitOperations(ByteArray byteArray) {
        // Test digit1 operations
        byte[] byteBuf = new byte[] {'1', '2', '3', 'a', '5'};
        char[] charBuf = new char[] {'4', '5', '6', 'b', '8'};

        assertEquals(1, byteArray.digit1(byteBuf, 0));
        assertEquals(2, byteArray.digit1(byteBuf, 1));
        assertEquals(3, byteArray.digit1(byteBuf, 2));
        assertEquals(-1, byteArray.digit1(byteBuf, 3)); // 'a' is not a digit
        assertEquals(5, byteArray.digit1(byteBuf, 4));

        assertEquals(4, byteArray.digit1(charBuf, 0));
        assertEquals(5, byteArray.digit1(charBuf, 1));
        assertEquals(6, byteArray.digit1(charBuf, 2));
        assertEquals(-1, byteArray.digit1(charBuf, 3)); // 'b' is not a digit
        assertEquals(8, byteArray.digit1(charBuf, 4));

        // Test digit2 operations
        assertEquals(12, byteArray.digit2(byteBuf, 0));
        assertEquals(23, byteArray.digit2(byteBuf, 1));
        assertEquals(-1, byteArray.digit2(byteBuf, 2)); // '3a' contains non-digit
        assertEquals(-1, byteArray.digit2(byteBuf, 3)); // 'a5' contains non-digit

        assertEquals(45, byteArray.digit2(charBuf, 0));
        assertEquals(56, byteArray.digit2(charBuf, 1));
        assertEquals(-1, byteArray.digit2(charBuf, 2)); // '6b' contains non-digit
        assertEquals(-1, byteArray.digit2(charBuf, 3)); // 'b8' contains non-digit

        // Test digit3 operations
        assertEquals(123, byteArray.digit3(byteBuf, 0));
        assertEquals(-1, byteArray.digit3(byteBuf, 1)); // '23a' contains non-digit

        assertEquals(456, byteArray.digit3(charBuf, 0));
        assertEquals(-1, byteArray.digit3(charBuf, 1)); // '56b' contains non-digit

        // Test digit4 operations - only test valid sequences
        byte[] valid4Byte = new byte[] {'1', '2', '3', '4', '5'};
        assertEquals(1234, byteArray.digit4(valid4Byte, 0));
        assertEquals(2345, byteArray.digit4(valid4Byte, 1));

        char[] valid4Char = new char[] {'2', '3', '4', '5', '6'};
        assertEquals(2345, byteArray.digit4(valid4Char, 0));
        assertEquals(3456, byteArray.digit4(valid4Char, 1));
    }
}
