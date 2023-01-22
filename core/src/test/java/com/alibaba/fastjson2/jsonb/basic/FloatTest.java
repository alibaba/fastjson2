package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatTest {
    /**
     * 0xb6
     */
    @Test
    public void testFloatIntNum() {
        for (int i = BC_INT32_NUM_MIN; i <= BC_INT32_NUM_MAX; i++) {
            float floatValue = (float) i;
            byte[] bytes = JSONB.toBytes(floatValue);
            assertEquals(2, bytes.length);
            assertEquals(BC_FLOAT_INT, bytes[0]);
            assertEquals(i, bytes[1]);

            Float parsed = (Float) JSONB.parse(bytes);
            assertEquals(parsed.floatValue(), floatValue);
        }
    }

    /**
     * 0xb6
     */
    @Test
    public void testFloatIntByte() {
        for (int i = INT32_BYTE_MIN; i <= INT32_BYTE_MAX; i++) {
            if (i >= BC_INT32_NUM_MIN && i <= BC_INT32_NUM_MAX) {
                continue;
            }
            float floatValue = i;

            byte[] bytes = JSONB.toBytes(floatValue);
            assertEquals(3, bytes.length);
            assertEquals(BC_FLOAT_INT, bytes[0]);
            byte b1 = bytes[1];
            byte b2 = bytes[2];

            int value = ((b1 - BC_INT32_BYTE_ZERO) << 8) + (b2 & 0xFF);
            assertEquals(i, value);

            Float parsed = (Float) JSONB.parse(bytes);
            assertEquals(parsed.floatValue(), floatValue);
        }
    }

    /**
     * 0xb6
     */
    @Test
    public void testIntShort() {
        for (int i = INT32_SHORT_MIN; i <= INT32_SHORT_MAX; i++) {
            if (i >= INT32_BYTE_MIN && i <= INT32_BYTE_MAX) {
                continue;
            }

            float floatValue = i;

            byte[] bytes = JSONB.toBytes(floatValue);
            assertEquals(4, bytes.length);
            assertEquals(BC_FLOAT_INT, bytes[0]);
            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];

            int value = ((b1 - BC_INT32_SHORT_ZERO) << 16)
                    + ((b2 & 0xFF) << 8)
                    + (b3 & 0xFF);
            assertEquals(i, value);

            Float parsed = (Float) JSONB.parse(bytes);
            assertEquals(parsed.floatValue(), floatValue);
        }
    }

    /**
     * 0xb7
     */
    @Test
    public void testFloatInt32() {
        int[] ints = new int[] {
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                INT32_SHORT_MIN - 1,
                INT32_SHORT_MAX + 1
        };

        for (int i = 0; i < ints.length; i++) {
            float floatValue = ints[i];
            byte[] bytes = JSONB.toBytes(floatValue);
            assertEquals(5, bytes.length);
            assertEquals(BC_FLOAT, bytes[0]);

            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];

            int int32Value =
                    ((b4 & 0xFF)) +
                            ((b3 & 0xFF) << 8) +
                            ((b2 & 0xFF) << 16) +
                            ((b1) << 24);
            assertEquals(floatValue, Float.intBitsToFloat(int32Value));

            Float parsed = (Float) JSONB.parse(bytes);
            assertEquals(parsed.floatValue(), floatValue);
        }
    }

    /**
     * 0xb7
     */
    @Test
    public void testFloat() {
        Random r = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            float floatValue = r.nextFloat();
            if (floatValue == (int) floatValue) {
                continue;
            }

            byte[] bytes = JSONB.toBytes(floatValue);
            assertEquals(5, bytes.length);
            assertEquals(BC_FLOAT, bytes[0]);

            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];
            int int32Value =
                    ((b4 & 0xFF)) +
                            ((b3 & 0xFF) << 8) +
                            ((b2 & 0xFF) << 16) +
                            ((b1) << 24);
            assertEquals(floatValue, Float.intBitsToFloat(int32Value));

            Float parsed = (Float) JSONB.parse(bytes);
            assertEquals(parsed.floatValue(), floatValue);
        }
    }
}
