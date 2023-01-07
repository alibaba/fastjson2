package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoubleTest {
    /**
     * 0xb2
     */
    @Test
    public void testDouble_zero() {
        double zero = 0;
        byte[] bytes = JSONB.toBytes(zero);
        assertEquals(1, bytes.length);
        assertEquals(BC_DOUBLE_NUM_0, bytes[0]);

        Double parsed = (Double) JSONB.parse(bytes);
        assertEquals(zero, parsed.doubleValue());
    }

    /**
     * 0xb3
     */
    @Test
    public void testDouble_one() {
        double one = 1;
        byte[] bytes = JSONB.toBytes(one);
        assertEquals(1, bytes.length);
        assertEquals(BC_DOUBLE_NUM_1, bytes[0]);

        Double parsed = (Double) JSONB.parse(bytes);
        assertEquals(one, parsed.doubleValue());
    }

    /**
     * 0xb4
     */
    @Test
    public void testDoubleLongNum() {
        for (long i = INT64_NUM_LOW_VALUE; i <= INT64_NUM_HIGH_VALUE; i++) {
            if (i == 0 || i == 1) {
                continue;
            }

            double doubleValue = i;

            byte[] bytes = JSONB.toBytes(doubleValue);
            assertEquals(2, bytes.length);
            assertEquals(BC_DOUBLE_LONG, bytes[0]);

            byte b1 = bytes[1];
            long longValue = INT64_NUM_LOW_VALUE + (b1 - BC_INT64_NUM_MIN);
            assertEquals(i, longValue);

            Double parsed = (Double) JSONB.parse(bytes);
            assertEquals(doubleValue, parsed.doubleValue());
        }
    }

    /**
     * 0xb4
     */
    @Test
    public void testDoubleLongByte() {
        for (long i = INT64_BYTE_MIN; i <= INT64_BYTE_MAX; i++) {
            if (i >= INT64_NUM_LOW_VALUE && i <= INT64_NUM_HIGH_VALUE) {
                continue;
            }

            double doubleValue = i;

            byte[] bytes = JSONB.toBytes(doubleValue);
            assertEquals(3, bytes.length);
            assertEquals(BC_DOUBLE_LONG, bytes[0]);
            byte b1 = bytes[1];
            byte b2 = bytes[2];

            long value = ((b1 - BC_INT64_BYTE_ZERO) << 8) + (b2 & 0xFF);
            assertEquals(i, value);

            Double parsed = (Double) JSONB.parse(bytes);
            assertEquals(doubleValue, parsed.doubleValue());
        }
    }

    /**
     * 0xb4
     */
    @Test
    public void testDoubleLongShort() {
        for (long i = INT64_SHORT_MIN; i <= INT64_SHORT_MAX; i++) {
            if (i >= INT64_BYTE_MIN && i <= INT64_BYTE_MAX) {
                continue;
            }

            double doubleValue = i;
            byte[] bytes = JSONB.toBytes(doubleValue);
            assertEquals(4, bytes.length);
            assertEquals(BC_DOUBLE_LONG, bytes[0]);
            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];

            long value = ((b1 - BC_INT64_SHORT_ZERO) << 16)
                    + ((b2 & 0xFF) << 8)
                    + (b3 & 0xFF);
            assertEquals(i, value);

            Double parsed = (Double) JSONB.parse(bytes);
            assertEquals(doubleValue, parsed.doubleValue());
        }
    }

    /**
     * 0xb4
     */
    @Test
    public void testDoubleLongInt() {
        long[] ints = new long[] {
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                INT64_SHORT_MIN - 1,
                INT64_SHORT_MAX + 1
        };

        for (int i = 0; i < ints.length; i++) {
            double doubleValue = ints[i];
            byte[] bytes = JSONB.toBytes(doubleValue);
            assertEquals(6, bytes.length);
            assertEquals(BC_DOUBLE_LONG, bytes[0]);
            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];
            byte b5 = bytes[5];

            assertEquals(BC_INT64_INT, b1);
            long int32Value = ((b5 & 0xFF)) +
                    ((b4 & 0xFF) << 8) +
                    ((b3 & 0xFF) << 16) +
                    ((b2) << 24);
            assertEquals(doubleValue, int32Value);

            Double parsed = (Double) JSONB.parse(bytes);
            assertEquals(doubleValue, parsed.doubleValue());
        }
    }

    /**
     * 0xb5
     */
    @Test
    public void testLong() {
        long[] ints = new long[] {
                Long.MIN_VALUE, Long.MAX_VALUE,
                Integer.MIN_VALUE - 1L,
                Integer.MAX_VALUE + 1L
        };

        for (int i = 0; i < ints.length; i++) {
            double doubleValue = ints[i];
            byte[] bytes = JSONB.toBytes(doubleValue);
            assertEquals(9, bytes.length);
            assertEquals(BC_DOUBLE, bytes[0]);

            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];
            byte b5 = bytes[5];
            byte b6 = bytes[6];
            byte b7 = bytes[7];
            byte b8 = bytes[8];
            long int64Value =
                    ((b8 & 0xFFL)) +
                            ((b7 & 0xFFL) << 8) +
                            ((b6 & 0xFFL) << 16) +
                            ((b5 & 0xFFL) << 24) +
                            ((b4 & 0xFFL) << 32) +
                            ((b3 & 0xFFL) << 40) +
                            ((b2 & 0xFFL) << 48) +
                            (((long) b1) << 56);
            assertEquals(doubleValue, Double.longBitsToDouble(int64Value));

            Double parsed = (Double) JSONB.parse(bytes);
            assertEquals(doubleValue, parsed.doubleValue());
        }
    }

    /**
     * 0xb5
     */
    @Test
    public void testDouble() {
        Random r = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            double doubleValue = r.nextDouble();
            if (doubleValue == (long) doubleValue) {
                continue;
            }

            byte[] bytes = JSONB.toBytes(doubleValue);
            assertEquals(9, bytes.length);
            assertEquals(BC_DOUBLE, bytes[0]);

            Double parsed = (Double) JSONB.parse(bytes);
            assertEquals(parsed.doubleValue(), doubleValue);
        }
    }
}
