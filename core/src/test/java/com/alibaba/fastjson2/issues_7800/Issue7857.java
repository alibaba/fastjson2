package com.alibaba.fastjson2.issues_7800;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class Issue7857 {
    @Test
    public void scaleOverflow() {
        for (int scale : new int[]{-2049, 2049, -67000000, 1 << 24, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            byte[] decimal = decimal(scale);
            assertRejected(decimal);

            byte[] nested = new byte[decimal.length + 2];
            nested[0] = BC_DECIMAL;
            System.arraycopy(decimal, 0, nested, 2, decimal.length);
            assertRejected(nested);

            byte[] nestedScale = new byte[decimal.length + 3];
            nestedScale[0] = BC_DECIMAL;
            System.arraycopy(decimal, 0, nestedScale, 1, decimal.length);
            nestedScale[nestedScale.length - 2] = BC_BIGINT_LONG;
            nestedScale[nestedScale.length - 1] = 1;
            assertRejected(nestedScale);
        }
    }

    @Test
    public void computedScaleOverflow() {
        assertRejected(new byte[]{
                (byte) 0xb9, 0x2d, (byte) 0xb9, (byte) 0xb9, (byte) 0xb9, (byte) 0xb1,
                (byte) 0xb9, 0x00, (byte) 0xbd, (byte) 0xbd, (byte) 0xbd, (byte) 0xbd,
                (byte) 0xbd, (byte) 0xbd
        });
    }

    @Test
    public void validScales() {
        for (int scale : new int[]{-2048, -2047, -10, -1, 0, 1, 10, 2047, 2048}) {
            for (BigInteger unscaled : new BigInteger[]{
                    BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(-123),
                    BigInteger.valueOf(Integer.MAX_VALUE), BigInteger.valueOf(Long.MAX_VALUE),
                    BigInteger.ONE.shiftLeft(80)
            }) {
                BigDecimal expected = new BigDecimal(unscaled, scale);
                byte[] bytes = JSONB.toBytes(expected);
                assertEquals(expected, JSONB.parse(bytes));
                assertEquals(expected, JSONB.parseObject(bytes, BigDecimal.class));
                byte[] scaleBytes = JSONB.toBytes(scale);
                byte[] unscaledBytes = JSONB.toBytes(unscaled);
                bytes = new byte[1 + scaleBytes.length + unscaledBytes.length];
                bytes[0] = BC_DECIMAL;
                System.arraycopy(scaleBytes, 0, bytes, 1, scaleBytes.length);
                System.arraycopy(unscaledBytes, 0, bytes, 1 + scaleBytes.length, unscaledBytes.length);
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected, reader.readNumber());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.toBigInteger(), reader.readBigInteger());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.intValue(), reader.readInt32Value());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.longValue(), reader.readInt64Value());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(expected.toString(), reader.readString());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    reader.skipValue();
                    assertEquals(bytes.length, reader.getOffset());
                }
            }
        }
    }

    private static byte[] decimal(int scale) {
        return new byte[]{
                BC_DECIMAL, BC_INT32,
                (byte) (scale >>> 24), (byte) (scale >>> 16), (byte) (scale >>> 8), (byte) scale,
                BC_BIGINT_LONG, 1
        };
    }

    private static void assertRejected(byte[] bytes) {
        assertThrows(JSONException.class, () -> JSONB.parse(bytes));
        for (Consumer<JSONReader> operation : operations()) {
            try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                JSONException error = assertThrows(JSONException.class, () -> operation.accept(reader));
                assertTrue(error.getMessage().startsWith("scale overflow : "), error.getMessage());
            }
        }
    }

    private static Iterable<Consumer<JSONReader>> operations() {
        return java.util.Arrays.asList(
                JSONReader::readAny,
                JSONReader::skipValue,
                JSONReader::readString,
                JSONReader::readInt64Value,
                JSONReader::readInt32Value,
                JSONReader::readFloatValue,
                JSONReader::readDoubleValue,
                JSONReader::readNumber,
                JSONReader::readBigDecimal,
                JSONReader::readBigInteger
        );
    }
}
