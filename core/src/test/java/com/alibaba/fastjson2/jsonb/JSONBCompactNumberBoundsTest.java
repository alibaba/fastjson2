package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("jsonb")
public class JSONBCompactNumberBoundsTest {
    @Test
    public void truncatedCompactInt32Byte() {
        byte[] bytes = {BC_INT32_BYTE_ZERO};
        assertMalformedAcrossNumberReaders(bytes);
        assertMalformedSlice(bytes);
    }

    @Test
    public void truncatedCompactInt32Short() {
        assertMalformedAcrossNumberReaders(new byte[]{BC_INT32_SHORT_ZERO});
        assertMalformedAcrossNumberReaders(new byte[]{BC_INT32_SHORT_ZERO, 0});
    }

    @Test
    public void truncatedCompactInt64Byte() {
        byte[] bytes = {BC_INT64_BYTE_ZERO};
        assertMalformedAcrossNumberReaders(bytes);
        assertMalformedSlice(bytes);
    }

    @Test
    public void truncatedCompactInt64Short() {
        assertMalformedAcrossNumberReaders(new byte[]{BC_INT64_SHORT_ZERO});
        assertMalformedAcrossNumberReaders(new byte[]{BC_INT64_SHORT_ZERO, 0});
    }

    @Test
    public void truncatedCompactLength() {
        assertThrows(JSONException.class, () -> JSONB.parse(new byte[]{BC_ARRAY, BC_INT32_BYTE_ZERO}));
        assertThrows(JSONException.class, () -> JSONB.parse(new byte[]{BC_STR_UTF8, BC_INT32_BYTE_ZERO}));
    }

    @Test
    public void truncatedCompactValuesInContainers() {
        assertThrows(JSONException.class, () -> JSONB.parse(new byte[]{
                (byte) (BC_ARRAY_FIX_MIN + 1), BC_INT32_BYTE_ZERO
        }));
        assertThrows(JSONException.class, () -> JSONB.parse(new byte[]{
                BC_OBJECT, BC_STR_ASCII_FIX_1, 'a', BC_INT32_BYTE_ZERO
        }));
    }

    @Test
    public void validCompactNumberBoundaries() {
        Number[] values = {
                -2048, 2047,
                -262144, 262143,
                -2048L, 2047L,
                -262144L, 262143L
        };
        for (Number value : values) {
            assertEquals(value, JSONB.parse(JSONB.toBytes(value)));
        }
    }

    private static void assertMalformedAcrossNumberReaders(byte[] bytes) {
        assertThrows(JSONException.class, () -> JSONB.parse(bytes));
        assertThrows(JSONException.class, () -> JSONB.parseObject(bytes, Integer.class));
        assertThrows(JSONException.class, () -> JSONB.parseObject(bytes, Long.class));
        assertThrows(JSONException.class, () -> JSONB.parseObject(bytes, BigInteger.class));
        assertThrows(JSONException.class, () -> JSONB.parseObject(bytes, BigDecimal.class));

        assertMalformed(bytes, JSONReader::readAny);
        assertMalformed(bytes, JSONReader::readInt32Value);
        assertMalformed(bytes, JSONReader::readInt32);
        assertMalformed(bytes, JSONReader::readInt64Value);
        assertMalformed(bytes, JSONReader::readInt64);
        assertMalformed(bytes, JSONReader::readNumber);
        assertMalformed(bytes, JSONReader::readBigInteger);
        assertMalformed(bytes, JSONReader::readBigDecimal);
        assertMalformed(bytes, JSONReader::readFloatValue);
        assertMalformed(bytes, JSONReader::readDoubleValue);
    }

    private static void assertMalformed(byte[] bytes, Function<JSONReader, ?> action) {
        JSONReader reader = JSONReader.ofJSONB(bytes);
        assertThrows(JSONException.class, () -> action.apply(reader));
    }

    private static void assertMalformedSlice(byte[] bytes) {
        byte[] backing = {bytes[0], 1};
        JSONReader reader = JSONReader.ofJSONB(backing, 0, 1);
        assertThrows(JSONException.class, reader::readAny);
    }
}
