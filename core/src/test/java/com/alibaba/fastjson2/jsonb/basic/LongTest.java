package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongTest {
    /**
     * 0xd8 ~ 0xef
     */
    @Test
    public void testInt64Num() {
        for (long i = INT64_NUM_LOW_VALUE; i <= INT64_NUM_HIGH_VALUE; i++) {
            byte[] bytes = JSONB.toBytes(i);
            assertEquals(1, bytes.length);
            byte b0 = bytes[0];
            long longValue = INT64_NUM_LOW_VALUE + (b0 - BC_INT64_NUM_MIN);
            assertEquals(i, longValue);

            Long parsed = (Long) JSONB.parse(bytes);
            assertEquals(i, parsed.longValue());
        }
    }

    /**
     * 0xc8 ~ 0xd7
     */
    @Test
    public void testLongByte() {
        for (long i = INT64_BYTE_MIN; i <= INT64_BYTE_MAX; i++) {
            if (i >= INT64_NUM_LOW_VALUE && i <= INT64_NUM_HIGH_VALUE) {
                continue;
            }

            byte[] bytes = JSONB.toBytes(i);
            assertEquals(2, bytes.length);
            byte b0 = bytes[0];
            byte b1 = bytes[1];

            long value = ((b0 - BC_INT64_BYTE_ZERO) << 8) + (b1 & 0xFF);
            assertEquals(i, value);

            Long parsed = (Long) JSONB.parse(bytes);
            assertEquals(i, parsed.longValue());
        }
    }

    /**
     * 0xc0 ~ 0xc7
     */
    @Test
    public void testLongShort() {
        for (long i = INT64_SHORT_MIN; i <= INT64_SHORT_MAX; i++) {
            if (i >= INT64_BYTE_MIN && i <= INT64_BYTE_MAX) {
                continue;
            }

            byte[] bytes = JSONB.toBytes(i);
            assertEquals(3, bytes.length);
            byte b0 = bytes[0];
            byte b1 = bytes[1];
            byte b2 = bytes[2];

            long value = ((b0 - BC_INT64_SHORT_ZERO) << 16)
                    + ((b1 & 0xFF) << 8)
                    + (b2 & 0xFF);
            assertEquals(i, value);

            Long parsed = (Long) JSONB.parse(bytes);
            assertEquals(i, parsed.longValue());
        }
    }

    /**
     * 0xbf
     */
    @Test
    public void testLongInt() {
        long[] ints = new long[] {
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                INT64_SHORT_MIN - 1,
                INT64_SHORT_MAX + 1
        };

        for (int i = 0; i < ints.length; i++) {
            long value = ints[i];
            byte[] bytes = JSONB.toBytes(value);
            assertEquals(5, bytes.length);
            byte b0 = bytes[0];
            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];

            assertEquals(BC_INT64_INT, b0);

            long int32Value = ((b4 & 0xFF)) +
                    ((b3 & 0xFF) << 8) +
                    ((b2 & 0xFF) << 16) +
                    ((b1) << 24);
            assertEquals(value, int32Value);

            Long parsed = (Long) JSONB.parse(bytes);
            assertEquals(value, parsed.longValue());
        }
    }

    /**
     * 0xbe
     */
    @Test
    public void testLong() {
        long[] ints = new long[] {
                Long.MIN_VALUE, Long.MAX_VALUE,
                Integer.MIN_VALUE - 1L,
                Integer.MAX_VALUE + 1L
        };

        for (int i = 0; i < ints.length; i++) {
            long value = ints[i];
            byte[] bytes = JSONB.toBytes(value);
            assertEquals(9, bytes.length);
            byte b0 = bytes[0];
            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];
            byte b5 = bytes[5];
            byte b6 = bytes[6];
            byte b7 = bytes[7];
            byte b8 = bytes[8];

            assertEquals(BC_INT64, b0);

            long int64Value =
                    ((b8 & 0xFFL)) +
                            ((b7 & 0xFFL) << 8) +
                            ((b6 & 0xFFL) << 16) +
                            ((b5 & 0xFFL) << 24) +
                            ((b4 & 0xFFL) << 32) +
                            ((b3 & 0xFFL) << 40) +
                            ((b2 & 0xFFL) << 48) +
                            (((long) b1) << 56);
            assertEquals(value, int64Value);

            Long parsed = (Long) JSONB.parse(bytes);
            assertEquals(value, parsed.longValue());
        }
    }
}
