package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecimalTest {
    @Test
    public void testDecimalLongNum() {
        for (long i = INT64_NUM_LOW_VALUE; i <= INT64_NUM_HIGH_VALUE; i++) {
            BigDecimal decimal = BigDecimal.valueOf(i);

            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(2, bytes.length);
            assertEquals(BC_DECIMAL_LONG, bytes[0]);

            byte b1 = bytes[1];
            long longValue = INT64_NUM_LOW_VALUE + (b1 - BC_INT64_NUM_MIN);
            assertEquals(i, longValue);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    @Test
    public void testDecimalLongByte() {
        for (long i = INT64_BYTE_MIN; i <= INT64_BYTE_MAX; i++) {
            if (i >= INT64_NUM_LOW_VALUE && i <= INT64_NUM_HIGH_VALUE) {
                continue;
            }

            BigDecimal decimal = BigDecimal.valueOf(i);

            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(3, bytes.length);
            assertEquals(BC_DECIMAL_LONG, bytes[0]);
            byte b1 = bytes[1];
            byte b2 = bytes[2];

            long value = ((b1 - BC_INT64_BYTE_ZERO) << 8) + (b2 & 0xFF);
            assertEquals(i, value);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    @Test
    public void testDecimalLongShort() {
        for (long i = INT64_SHORT_MIN; i <= INT64_SHORT_MAX; i++) {
            if (i >= INT64_BYTE_MIN && i <= INT64_BYTE_MAX) {
                continue;
            }

            BigDecimal decimal = BigDecimal.valueOf(i);
            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(4, bytes.length);
            assertEquals(BC_DECIMAL_LONG, bytes[0]);
            byte b1 = bytes[1];
            byte b2 = bytes[2];
            byte b3 = bytes[3];

            long value = ((b1 - BC_INT64_SHORT_ZERO) << 16)
                    + ((b2 & 0xFF) << 8)
                    + (b3 & 0xFF);
            assertEquals(i, value);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    @Test
    public void testDoubleLongInt() {
        long[] ints = new long[]{
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                INT64_SHORT_MIN - 1,
                INT64_SHORT_MAX + 1
        };

        for (int i = 0; i < ints.length; i++) {
            BigDecimal decimal = BigDecimal.valueOf(ints[i]);
            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(6, bytes.length);
            assertEquals(BC_DECIMAL_LONG, bytes[0]);
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
            assertEquals(decimal.longValue(), int32Value);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    @Test
    public void testDecimalLong() {
        long[] ints = new long[]{
                Long.MIN_VALUE, Long.MAX_VALUE,
                Integer.MIN_VALUE - 1L,
                Integer.MAX_VALUE + 1L
        };

        for (int i = 0; i < ints.length; i++) {
            BigDecimal decimal = BigDecimal.valueOf(ints[i]);
            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(10, bytes.length);
            assertEquals(BC_DECIMAL_LONG, bytes[0]);
            assertEquals(BC_INT64, bytes[1]);
            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];
            byte b5 = bytes[5];
            byte b6 = bytes[6];
            byte b7 = bytes[7];
            byte b8 = bytes[8];
            byte b9 = bytes[9];
            long int64Value =
                    ((b9 & 0xFFL)) +
                            ((b8 & 0xFFL) << 8) +
                            ((b7 & 0xFFL) << 16) +
                            ((b6 & 0xFFL) << 24) +
                            ((b5 & 0xFFL) << 32) +
                            ((b4 & 0xFFL) << 40) +
                            ((b3 & 0xFFL) << 48) +
                            (((long) b2) << 56);
            assertEquals(decimal.longValue(), int64Value);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    /**
     * 0xb9  int32_num
     */
    @Test
    public void test_decimal_unscaledValue_int_num() {
        for (int i = BC_INT32_NUM_MIN; i <= BC_INT32_NUM_MAX; i++) {
            int unscaledValue = i;
            BigDecimal decimal = BigDecimal.valueOf(unscaledValue, 1);

            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(3, bytes.length);
            assertEquals(BC_DECIMAL, bytes[0]);
            assertEquals(1, bytes[1]); // scale

            assertEquals(unscaledValue, bytes[2]); // unscaledValue
            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    /**
     * 0xb9 int32_byte
     */
    @Test
    public void test_decimal_unscaledValue_int_byte() {
        for (int i = INT32_BYTE_MIN; i <= INT32_BYTE_MAX; i++) {
            if (i >= BC_INT32_NUM_MIN && i <= BC_INT32_NUM_MAX) {
                continue;
            }

            int unscaledValue = i;
            BigDecimal decimal = BigDecimal.valueOf(unscaledValue, 1);

            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(4, bytes.length);
            assertEquals(BC_DECIMAL, bytes[0]);
            assertEquals(1, bytes[1]); // scale

            byte b2 = bytes[2];
            byte b3 = bytes[3];
            int value = ((b2 - BC_INT32_BYTE_ZERO) << 8) + (b3 & 0xFF);
            assertEquals(unscaledValue, value);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    /**
     * 0xb9 int32_short
     */
    @Test
    public void test_decimal_unscaledValue_int_short() {
        for (int i = INT32_SHORT_MIN; i <= INT32_SHORT_MAX; i++) {
            if (i >= INT32_BYTE_MIN && i <= INT32_BYTE_MAX) {
                continue;
            }

            int unscaledValue = i;
            BigDecimal decimal = BigDecimal.valueOf(unscaledValue, 1);

            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(5, bytes.length);
            assertEquals(BC_DECIMAL, bytes[0]);
            assertEquals(1, bytes[1]); // scale

            byte b2 = bytes[2];
            byte b3 = bytes[3];
            byte b4 = bytes[4];

            int value = ((b2 - BC_INT32_SHORT_ZERO) << 16)
                    + ((b3 & 0xFF) << 8)
                    + (b4 & 0xFF);
            assertEquals(unscaledValue, value);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    /**
     * 0xb9 int32
     */
    @Test
    public void test_decimal_unscaledValue_int() {
        int[] ints = new int[]{
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                INT32_SHORT_MIN - 1,
                INT32_SHORT_MAX + 1
        };

        for (int i = 0; i < ints.length; i++) {
            int unscaledValue = ints[i];
            BigDecimal decimal = BigDecimal.valueOf(unscaledValue, 1);

            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(7, bytes.length);
            assertEquals(BC_DECIMAL, bytes[0]);
            assertEquals(1, bytes[1]); // scale

            byte b3 = bytes[3];
            byte b4 = bytes[4];
            byte b5 = bytes[5];
            byte b6 = bytes[6];

            int value = ((b6 & 0xFF)) +
                    ((b5 & 0xFF) << 8) +
                    ((b4 & 0xFF) << 16) +
                    ((b3) << 24);

            assertEquals(unscaledValue, value);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    /**
     * 0xb9 int64
     */
    @Test
    public void test_decimal_unscaledValue_long() {
        long[] ints = new long[]{
                Long.MIN_VALUE,
                Long.MAX_VALUE,
                Integer.MIN_VALUE - 1L,
                Integer.MAX_VALUE + 1L,
        };

        for (int i = 0; i < ints.length; i++) {
            long unscaledValue = ints[i];
            BigDecimal decimal = BigDecimal.valueOf(unscaledValue, 1);

            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(11, bytes.length);
            assertEquals(BC_DECIMAL, bytes[0]);
            assertEquals(1, bytes[1]); // scale
            assertEquals(BC_INT64, bytes[2]);
            byte b3 = bytes[3];
            byte b4 = bytes[4];
            byte b5 = bytes[5];
            byte b6 = bytes[6];
            byte b7 = bytes[7];
            byte b8 = bytes[8];
            byte b9 = bytes[9];
            byte b10 = bytes[10];

            long int64Value =
                    ((b10 & 0xFFL)) +
                            ((b9 & 0xFFL) << 8) +
                            ((b8 & 0xFFL) << 16) +
                            ((b7 & 0xFFL) << 24) +
                            ((b6 & 0xFFL) << 32) +
                            ((b5 & 0xFFL) << 40) +
                            ((b4 & 0xFFL) << 48) +
                            (((long) b3) << 56);

            assertEquals(unscaledValue, int64Value);

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }

    /**
     * 0xb9 bigint
     */
    @Test
    public void test_decimal_unscaledValue_bigint() {
        BigInteger[] ints = new BigInteger[] {
                new BigInteger("-92233720368547758080"),
                new BigInteger("92233720368547758080")
        };

        for (int i = 0; i < ints.length; i++) {
            BigInteger unscaledValue = ints[i];
            BigDecimal decimal = new BigDecimal(unscaledValue, 1);

            byte[] bytes = JSONB.toBytes(decimal);
            assertEquals(13, bytes.length);
            assertEquals(BC_DECIMAL, bytes[0]);
            assertEquals(1, bytes[1]); // scale
            assertEquals(BC_BIGINT, bytes[2]);

            assertEquals(9, bytes[3]); // bigintBytes length
            byte[] bigIntBytes = Arrays.copyOfRange(bytes, 4, bytes.length);
            assertEquals(unscaledValue, new BigInteger(bigIntBytes));

            BigDecimal parsed = (BigDecimal) JSONB.parse(bytes);
            assertEquals(decimal, parsed);
        }
    }
}
