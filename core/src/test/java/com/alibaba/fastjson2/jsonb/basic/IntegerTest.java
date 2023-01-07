package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerTest {
    /**
     * 0xf0 ~ 02f
     */
    @Test
    public void testIntNum() {
        for (int i = BC_INT32_NUM_MIN; i <= BC_INT32_NUM_MAX; i++) {
            byte[] bytes = JSONB.toBytes(i);
            assertEquals(1, bytes.length);
            assertEquals(i, bytes[0]);

            Integer parsed = (Integer) JSONB.parse(bytes);
            assertEquals(i, parsed.intValue());
        }
    }

    /**
     * 0x30 ~ 0x3f
     */
    @Test
    public void testIntByte() {
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

            Integer parsed = (Integer) JSONB.parse(bytes);
            assertEquals(value, parsed.intValue());
        }
    }

    /**
     * 0x40 ~ 0x47
     */
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

            Integer parsed = (Integer) JSONB.parse(bytes);
            assertEquals(value, parsed.intValue());
        }
    }

    /**
     * 0x48
     */
    @Test
    public void testInt32() {
        int[] ints = new int[] {
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                INT32_SHORT_MIN - 1,
                INT32_SHORT_MAX + 1
        };

        for (int i = 0; i < ints.length; i++) {
            int value = ints[i];
            byte[] bytes = JSONB.toBytes(value);
            assertEquals(5, bytes.length);
            byte b0 = bytes[0];
            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];

            assertEquals(BC_INT32, b0);

            int int32Value = ((b4 & 0xFF)) +
                    ((b3 & 0xFF) << 8) +
                    ((b2 & 0xFF) << 16) +
                    ((b1) << 24);
            assertEquals(value, int32Value);

            Integer parsed = (Integer) JSONB.parse(bytes);
            assertEquals(value, parsed.intValue());
        }
    }
}
