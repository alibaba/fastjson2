package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerTest {
    static final int INT32_SHORT_MIN = -0x40000; // -262144
    static final int INT32_SHORT_MAX = 0x3ffff;  // 262143

    static final int INT32_BYTE_MIN = -0x800; // -2048
    static final int INT32_BYTE_MAX = 0x7ff;  // 2047
    static final byte BC_INT32_SHORT_ZERO = 68;
    static final byte BC_INT32_BYTE_ZERO = 56;

    @Test
    public void testIntNum() {
        final byte BC_INT32_NUM_MIN = -16; // 0xf0
        final byte BC_INT32_NUM_MAX = 47;  // 0x2f

        for (int i = BC_INT32_NUM_MIN; i <= BC_INT32_NUM_MAX; i++) {
            byte[] bytes = JSONB.toBytes(i);
            assertEquals(1, bytes.length);
            assertEquals(i, bytes[0]);
        }
    }

    @Test
    public void testIntByte() {
        final byte BC_INT32_NUM_MIN = -16; // 0xf0
        final byte BC_INT32_NUM_MAX = 47;  // 0x2f

        for (int i = INT32_BYTE_MIN; i <= INT32_BYTE_MAX; i++) {
            if (i >= BC_INT32_NUM_MIN && i <= BC_INT32_NUM_MAX) {
                continue;
            }

            byte[] bytes = JSONB.toBytes(i);
            assertEquals(2, bytes.length);
            byte b0 = bytes[0];
            byte b1 = bytes[1];

            int value = ((b0 - BC_INT32_BYTE_ZERO) << 8) + (b1 & 0xFF);
            assertEquals(i, value);
        }
    }

    @Test
    public void testIntShort() {
        for (int i = INT32_SHORT_MIN; i <= INT32_SHORT_MAX; i++) {
            if (i >= INT32_BYTE_MIN && i <= INT32_BYTE_MAX) {
                continue;
            }

            byte[] bytes = JSONB.toBytes(i);
            assertEquals(3, bytes.length);
            byte b0 = bytes[0];
            byte b1 = bytes[1];
            byte b2 = bytes[2];

            int value = ((b0 - BC_INT32_SHORT_ZERO) << 16)
                    + ((b1 & 0xFF) << 8)
                    + (b2 & 0xFF);
            assertEquals(i, value);
        }
    }
}
