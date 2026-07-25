package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
@Tag("jsonb")
public class Issue7669 {
    @Test
    public void testBigIntDeclaredLengthOOM() {
        // 0xBB = BC_BIGINT, 0x48 = BC_INT32, 0x7FFFFFFF = Integer.MAX_VALUE
        byte[] payload = {
                (byte) 0xBB,
                (byte) 0x48,
                (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertThrows(JSONException.class, () -> JSONB.parse(payload));
    }

    @Test
    public void testBigIntNegativeLength() {
        // 0xBB = BC_BIGINT, 0x48 = BC_INT32, 0xFFFFFFFF = -1
        byte[] payload = {
                (byte) 0xBB,
                (byte) 0x48,
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertThrows(JSONException.class, () -> JSONB.parse(payload));
    }

    @Test
    public void testBigIntValidPayloadStillWorks() {
        // Verify legitimate BigInteger values still round-trip correctly
        java.math.BigInteger value = java.math.BigInteger.valueOf(Long.MAX_VALUE).multiply(java.math.BigInteger.TEN);
        byte[] jsonbBytes = JSONB.toBytes(value);
        java.math.BigInteger result = (java.math.BigInteger) JSONB.parse(jsonbBytes);
        org.junit.jupiter.api.Assertions.assertEquals(value, result);
    }
}
