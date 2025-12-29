package com.alibaba.fastjson2.internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class ByteArrayTest {
    static ByteArray[] primitiveFunctionFactories() {
        return new ByteArray[] {
                new ByteArray(),
                new ByteArrayUnsafe()
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
}
