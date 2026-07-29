package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
@Tag("jsonb")
public class Issue7669 {
    // 0xBB = BC_BIGINT, 0x48 = BC_INT32, 0x7FFFFFFF = Integer.MAX_VALUE
    static final byte[] PAYLOAD_MAX_LEN = {
            (byte) 0xBB,
            (byte) 0x48,
            (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0xBB = BC_BIGINT, 0x48 = BC_INT32, 0xFFFFFFFF = -1
    static final byte[] PAYLOAD_NEG_LEN = {
            (byte) 0xBB,
            (byte) 0x48,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0x91 = BC_BINARY, 0x48 = BC_INT32, 0x0FFFFFFF = 256MB-1 (passes readLength cap, exceeds buffer)
    static final byte[] BINARY_PAYLOAD_MAX_LEN = {
            (byte) 0x91,
            (byte) 0x48,
            (byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0x91 = BC_BINARY, 0x48 = BC_INT32, 0xFFFFFFFF = -1
    static final byte[] BINARY_PAYLOAD_NEG_LEN = {
            (byte) 0x91,
            (byte) 0x48,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    @Test
    public void testBigIntDeclaredLengthOOM() {
        assertThrows(JSONException.class, () -> JSONB.parse(PAYLOAD_MAX_LEN));
    }

    @Test
    public void testBigIntNegativeLength() {
        assertThrows(JSONException.class, () -> JSONB.parse(PAYLOAD_NEG_LEN));
    }

    @Test
    public void testBigIntDeclaredLengthOOM_readBigInteger() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(PAYLOAD_MAX_LEN, BigInteger.class));
    }

    @Test
    public void testBigIntDeclaredLengthOOM_readString() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(PAYLOAD_MAX_LEN, String.class));
    }

    @Test
    public void testBigIntDeclaredLengthOOM_readNumber() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(PAYLOAD_MAX_LEN, Number.class));
    }

    @Test
    public void testBigIntValidPayloadStillWorks() {
        BigInteger value = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN);
        byte[] jsonbBytes = JSONB.toBytes(value);
        BigInteger result = (BigInteger) JSONB.parse(jsonbBytes);
        assertEquals(value, result);
    }

    @Test
    public void testBinaryDeclaredLengthOOM() {
        assertThrows(JSONException.class, () -> JSONB.parse(BINARY_PAYLOAD_MAX_LEN));
    }

    @Test
    public void testBinaryNegativeLength() {
        assertThrows(JSONException.class, () -> JSONB.parse(BINARY_PAYLOAD_NEG_LEN));
    }

    @Test
    public void testReadBinaryDeclaredLengthOOM() {
        // Explicit readBinary() path
        try (JSONReader reader = JSONReader.ofJSONB(BINARY_PAYLOAD_MAX_LEN)) {
            assertThrows(JSONException.class, reader::readBinary);
        }
    }

    @Test
    public void testBinaryValidPayloadStillWorks() {
        byte[] value = {1, 2, 3, 4, 5};
        byte[] jsonbBytes = JSONB.toBytes(value);
        byte[] result = (byte[]) JSONB.parse(jsonbBytes);
        assertArrayEquals(value, result);
    }
}
