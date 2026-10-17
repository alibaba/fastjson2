package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONValidator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // --- BC_ARRAY declared-length OOM path (same class as #7669, fixed by this PR) ---

    // 0xA4 = BC_ARRAY, 0x48 = BC_INT32, 0x7FFFFFFF = Integer.MAX_VALUE
    static final byte[] ARRAY_PAYLOAD_MAX_LEN = {
            (byte) 0xA4,
            (byte) 0x48,
            (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0xA4 = BC_ARRAY, 0x48 = BC_INT32, 0xFFFFFFFF = -1
    static final byte[] ARRAY_PAYLOAD_NEG_LEN = {
            (byte) 0xA4,
            (byte) 0x48,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0x91 = BC_BINARY (routed through startArray() via typed array deserialization),
    // 0x48 = BC_INT32, 0x7FFFFFFF = Integer.MAX_VALUE
    static final byte[] BINARY_ARRAY_PAYLOAD_MAX_LEN = {
            (byte) 0x91,
            (byte) 0x48,
            (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    @Test
    public void testArrayDeclaredLengthOOM_int() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_PAYLOAD_MAX_LEN, int[].class));
    }

    @Test
    public void testArrayDeclaredLengthOOM_long() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_PAYLOAD_MAX_LEN, long[].class));
    }

    @Test
    public void testArrayDeclaredLengthOOM_string() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_PAYLOAD_MAX_LEN, String[].class));
    }

    @Test
    public void testArrayNegativeLength() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_PAYLOAD_NEG_LEN, int[].class));
    }

    @Test
    public void testArrayDeclaredLengthOOM_binaryBranch() {
        // The BC_BINARY branch of startArray() received the same checkArrayLen guard;
        // a crafted payload must be rejected through this path too (parseObject typed
        // array deserialization routes it through startArray()).
        assertThrows(JSONException.class, () -> JSONB.parseObject(BINARY_ARRAY_PAYLOAD_MAX_LEN, int[].class));
    }

    @Test
    public void testArrayValidPayloadStillWorks() {
        // ARRAY_FIX_LEN = 15: arrays with <= 15 elements are encoded as BC_ARRAY_FIX
        // and bypass startArray()'s checkLength entirely. Use 32 elements to force
        // BC_ARRAY encoding so this test actually exercises the guarded path.
        int[] value = new int[32];
        for (int i = 0; i < value.length; i++) {
            value[i] = i;
        }
        byte[] jsonbBytes = JSONB.toBytes(value);
        int[] result = JSONB.parseObject(jsonbBytes, int[].class);
        assertArrayEquals(value, result);
    }

    // --- R2-2 hardening: pin the position-relative bound (len > end - offset),
    // not an absolute bound. The malformed BC_ARRAY below is NOT at the buffer
    // end: an outer BC_ARRAY_FIX (0x95, 1 element) wraps an inner BC_ARRAY (0xA4)
    // declaring 5 elements while only 1 byte (0xF0) remains. A guard that
    // degenerates to `len > end` would let this slip through and later die with
    // ArrayIndexOutOfBoundsException instead of JSONException.
    // 0x95 = BC_ARRAY_FIX(1 elem), 0xA4 = BC_ARRAY, 0x48 = BC_INT32,
    // 0x00000005 = declares 5 elements, 0xF0 = 1 trailing byte
    static final byte[] NESTED_ARRAY_BAD_LEN = {
            (byte) 0x95,
            (byte) 0xA4,
            (byte) 0x48,
            0x00, 0x00, 0x00, 0x05,
            (byte) 0xF0
    };

    @Test
    public void testArrayDeclaredLengthNotAtBufferEnd() {
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(NESTED_ARRAY_BAD_LEN, int[][].class));
    }

    // --- R2-1: the same declared-length pre-allocation OOM class, but through the
    // untyped/nested BC_ARRAY decode paths that never call startArray():
    // readAny() (default branch), readObject() (map-value branch), readArray()
    // (element branch). They read the length via readLength() and pre-allocate
    // new JSONArray(len)/new ArrayList(len) with no remaining-buffer check.
    // This change folds the same checkLength guard into those three inline sites.

    // 0xA4 = BC_ARRAY, 0x48 = BC_INT32, 0x0FFFFFFF = 268,435,455 elements
    static final byte[] UNTYPED_ARRAY_MAX_LEN = {
            (byte) 0xA4,
            (byte) 0x48,
            (byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    /**
     * Corrupt the first BC_ARRAY (0xA4) + BC_INT32 (0x48) length field found in
     * the payload to a huge value, so the declared length vastly exceeds the
     * remaining buffer. Used to exercise the untyped/nested readLength() sites.
     */
    private static void corruptFirstArrayLength(byte[] bytes) {
        for (int i = 0; i + 5 < bytes.length; i++) {
            if ((bytes[i] & 0xFF) == 0xA4) {  // BC_ARRAY
                // Rewrite the array's leading byte as BC_INT32 (0x48) so the inline
                // readLength() site treats it as a 4-byte length-type, then overwrite
                // those 4 bytes with 0x0FFFFFFF (268,435,455) — far larger than the
                // remaining buffer — to exercise the checkLength guard placed at the
                // inline readLength() decode site (readObject() map-value branch).
                // The corrupt target is the inner array value, not the (BC_OBJECT)
                // root, because the root is never encoded as BC_ARRAY.
                bytes[i + 1] = (byte) 0x48;  // BC_INT32
                bytes[i + 2] = (byte) 0x0F;
                bytes[i + 3] = (byte) 0xFF;
                bytes[i + 4] = (byte) 0xFF;
                bytes[i + 5] = (byte) 0xFF;
                return;
            }
        }
        throw new IllegalStateException("BC_ARRAY not found in payload");
    }

    @Test
    public void testUntypedArrayDeclaredLengthOOM_readAny() {
        // JSONB.parse with no target type dispatches to readAny(); its BC_ARRAY
        // branch reads the length via readLength() and pre-allocates
        // new JSONArray(len) — now guarded by checkLength. Pin the guard's own
        // message: without the guard a downstream "readAny overflow" JSONException
        // would also satisfy a type-only assertion, letting the fix rot silently.
        JSONException ex = assertThrows(JSONException.class, () -> JSONB.parse(UNTYPED_ARRAY_MAX_LEN));
        assertTrue(ex.getMessage().contains("array length out of range"));
    }

    @Test
    public void testUntypedArrayDeclaredLengthOOM_readObject() {
        // A map whose value is an int[] (32 elems -> BC_ARRAY + BC_INT32 length).
        // Corrupt the declared length, then parse via the untyped overload:
        // JSONB.parseObject(bytes, Object.class) short-circuits through readAny()
        // (the Object.class fast path) and would NOT exercise the readObject()
        // map-value guard at JSONReaderJSONB.java:407 — the untyped overload does.
        Map<String, Object> m = new HashMap<>();
        m.put("k", new int[32]);
        byte[] bytes = JSONB.toBytes(m);
        corruptFirstArrayLength(bytes);
        JSONException ex = assertThrows(JSONException.class, () -> JSONB.parseObject(bytes));
        assertTrue(ex.getMessage().contains("array length out of range"));
    }

    @Test
    public void testUntypedArrayDeclaredLengthOOM_readArray() {
        // Top-level JSONB.parseArray dispatches to readArray(); a nested element
        // that is itself a BC_ARRAY must have its declared length (read via the
        // inline readLength() at the element branch, line ~1099) rejected by
        // checkLength.
        // 0xA4 = BC_ARRAY (outer, 1 element), 0x01 = outer declared length,
        // 0xA4 = BC_ARRAY (inner element), 0x48 = BC_INT32 length-type,
        // 0x0FFFFFFF = declares 268,435,455 inner elements (exceeds buffer).
        byte[] bytes = {
                (byte) 0xA4, 0x01,
                (byte) 0xA4, (byte) 0x48,
                (byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertThrows(JSONException.class, () -> JSONB.parseArray(bytes));
    }

    // --- R2-5: skipValue()/skipName() consumed stream-declared lengths with no
    // remaining-buffer check — the same defect class this PR closes elsewhere.
    // The PR's checkLength/checkArrayLen guards are folded into the skip arms so
    // a crafted payload fails as JSONException instead of AIOOBE, a rewind loop,
    // or a billion-iteration spin.

    // 0x91 = BC_BINARY, 0x48 = BC_INT32, 0x7FFFFFFF = Integer.MAX_VALUE.
    // skipValue's BC_BINARY arm reads the length via readInt32Value(), which has
    // no 256MB cap — pin that the shared guard fires on the raw declared value.
    static final byte[] SKIP_BINARY_MAX_LEN = {
            (byte) 0x91,
            (byte) 0x48,
            (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0x79 = BC_STR_ASCII, 0x48 = BC_INT32, 0xFFFFFFFA = -6
    static final byte[] SKIP_STR_NEG_LEN = {
            (byte) 0x79,
            (byte) 0x48,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFA
    };

    // 0xA6 = BC_OBJECT, 0x4A = BC_STR_ASCII_FIX(1) name "x" (0x78),
    // 0x79 = BC_STR_ASCII, 0x48 = BC_INT32, 0xFFFFFFFA = -6: before the fix
    // offset rewound onto the type byte and the object loop re-dispatched the
    // same bytes forever (probe-verified hang in review R2-5, scenario 2).
    static final byte[] SKIP_OBJECT_NEG_ITEM_LEN = {
            (byte) 0xA6,
            (byte) 0x4A, 0x78,
            (byte) 0x79,
            (byte) 0x48,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFA
    };

    // 0xA6 = BC_OBJECT, 0x4A = fix-str(1) "x", 0xB1 = BC_TRUE — one complete
    // entry, then the stream ends with no BC_OBJECT_END: the loop-top read must
    // fail as JSONException, not AIOOBE.
    static final byte[] SKIP_OBJECT_TRUNCATED = {
            (byte) 0xA6,
            (byte) 0x4A, 0x78,
            (byte) 0xB1
    };

    // 0x96 = BC_ARRAY_FIX(2 elems), 0xBE = BC_INT64 (fixed 9 bytes, 2 remain)
    static final byte[] SKIP_ARRAY_TRUNCATED = {
            (byte) 0x96,
            (byte) 0xBE, 0x00, 0x00
    };

    // 0x79 = BC_STR_ASCII, 0x48 = BC_INT32, 0x0FFFFFFF = 256MB-1 (passes the
    // readLength() cap, exceeds the remaining buffer)
    static final byte[] SKIP_NAME_MAX_LEN = {
            (byte) 0x79,
            (byte) 0x48,
            (byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0xA6 = BC_OBJECT, 0x4A = fix-str(1) unknown field "x",
    // 0x91 = BC_BINARY, 0x48 = BC_INT32, 0x7FFFFFFF, 0xA5 = BC_OBJECT_END
    static final byte[] BEAN_UNKNOWN_FIELD_BINARY_MAX_LEN = {
            (byte) 0xA6,
            (byte) 0x4A, 0x78,
            (byte) 0x91,
            (byte) 0x48,
            (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xA5
    };

    // 0xA6 = BC_OBJECT, 0x4A = fix-str(1) "x", 0xA4 = BC_ARRAY, then EOF:
    // the readObject() map-value array branch read bytes[offset] for the item
    // type before any bounds check (R2-4).
    static final byte[] MAP_VALUE_ARRAY_TRUNCATED = {
            (byte) 0xA6,
            (byte) 0x4A, 0x78,
            (byte) 0xA4
    };

    @Test
    public void testSkipValueBinaryDeclaredLength() {
        try (JSONReader reader = JSONReader.ofJSONB(SKIP_BINARY_MAX_LEN)) {
            JSONException ex = assertThrows(JSONException.class, reader::skipValue);
            assertTrue(ex.getMessage().contains("BC_BINARY length out of range"));
        }
    }

    @Test
    public void testSkipValueStringNegativeLength() {
        try (JSONReader reader = JSONReader.ofJSONB(SKIP_STR_NEG_LEN)) {
            JSONException ex = assertThrows(JSONException.class, reader::skipValue);
            assertTrue(ex.getMessage().contains("string length out of range"));
        }
    }

    @Test
    public void testSkipValueObjectNegativeItemLength() {
        try (JSONReader reader = JSONReader.ofJSONB(SKIP_OBJECT_NEG_ITEM_LEN)) {
            JSONException ex = assertThrows(JSONException.class, reader::skipValue);
            assertTrue(ex.getMessage().contains("object item length out of range"));
        }
    }

    @Test
    public void testSkipValueObjectTruncated() {
        try (JSONReader reader = JSONReader.ofJSONB(SKIP_OBJECT_TRUNCATED)) {
            JSONException ex = assertThrows(JSONException.class, reader::skipValue);
            assertTrue(ex.getMessage().contains("truncated object"));
        }
    }

    @Test
    public void testSkipValueArrayDeclaredLength() {
        try (JSONReader reader = JSONReader.ofJSONB(ARRAY_PAYLOAD_MAX_LEN)) {
            JSONException ex = assertThrows(JSONException.class, reader::skipValue);
            assertTrue(ex.getMessage().contains("array length out of range"));
        }
    }

    @Test
    public void testSkipValueArrayTruncated() {
        try (JSONReader reader = JSONReader.ofJSONB(SKIP_ARRAY_TRUNCATED)) {
            JSONException ex = assertThrows(JSONException.class, reader::skipValue);
            assertTrue(ex.getMessage().contains("truncated array"));
        }
    }

    @Test
    public void testSkipNameDeclaredLength() {
        try (JSONReader reader = JSONReader.ofJSONB(SKIP_NAME_MAX_LEN)) {
            JSONException ex = assertThrows(JSONException.class, reader::skipName);
            assertTrue(ex.getMessage().contains("field name length out of range"));
        }
    }

    @Test
    public void testUnknownFieldSkipDeclaredLength() {
        // review R2-5 scenario 1: bean deserialization skips the unknown field
        // "x" through skipValue(); the declared 0x7FFFFFFF binary must fail as
        // JSONException, not ArrayIndexOutOfBoundsException.
        assertThrows(JSONException.class,
                () -> JSONB.parseObject(BEAN_UNKNOWN_FIELD_BINARY_MAX_LEN, Bean.class));
    }

    @Test
    public void testValidatorRejectsMalformedSkipPayload() {
        // review R2-5 scenario 2 reproduced through the validator path.
        try (JSONReader reader = JSONReader.ofJSONB(SKIP_OBJECT_NEG_ITEM_LEN)) {
            assertFalse(JSONValidator.from(reader).validate());
        }
    }

    @Test
    public void testReadObjectArrayValueTruncated() {
        // JSONB.parse routes the top-level object through readAny()'s inline
        // object handling; its BC_ARRAY arm must reject a stream that ends
        // right after the BC_ARRAY type byte.
        assertThrows(JSONException.class, () -> JSONB.parse(MAP_VALUE_ARRAY_TRUNCATED));
    }

    @Test
    public void testReadObjectArrayValueTruncated_readObjectBranch() {
        // The untyped parseObject overload routes through readObject(); its
        // map-value array branch (R2-4) must fail the same truncated stream as
        // JSONException, not AIOOBE.
        assertThrows(JSONException.class, () -> JSONB.parseObject(MAP_VALUE_ARRAY_TRUNCATED));
    }

    @Test
    public void testSkipValueValidPayloadStillWorks() {
        Map<String, Object> m = new HashMap<>();
        m.put("bin", new byte[]{1, 2, 3});
        m.put("str", "hello");
        m.put("arr", new int[]{1, 2, 3});
        Map<String, Object> nested = new HashMap<>();
        nested.put("k", "v");
        m.put("obj", nested);
        byte[] bytes = JSONB.toBytes(m);
        try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
            reader.skipValue();
            assertTrue(reader.isEnd());
        }
    }

    public static class Bean {
        public int a;
    }
}
