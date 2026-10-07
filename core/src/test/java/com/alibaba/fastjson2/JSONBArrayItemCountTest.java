package com.alibaba.fastjson2;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("regression")
@Tag("jsonb")
public class JSONBArrayItemCountTest {
    // 0xA4 = BC_ARRAY, 0x48 = BC_INT32, 0x7FFFFFFF = Integer.MAX_VALUE
    static final byte[] ARRAY_MAX_CNT = {
            (byte) 0xA4,
            (byte) 0x48,
            (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0x0FFFFFFF stays under readLength()'s 256MB cap but still exceeds the input
    static final byte[] ARRAY_LARGE_CNT = {
            (byte) 0xA4,
            (byte) 0x48,
            (byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0xFFFFFFFF = -1
    static final byte[] ARRAY_NEG_CNT = {
            (byte) 0xA4,
            (byte) 0x48,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
    };

    // 0xA6 = BC_OBJECT, "a" -> array with declared count 0x0FFFFFFF, 0xA5 = BC_OBJECT_END
    static final byte[] OBJECT_ARRAY_LARGE_CNT = {
            (byte) 0xA6,
            (byte) 0x4A, (byte) 0x61,
            (byte) 0xA4, (byte) 0x48, (byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xA5
    };

    @Test
    public void testInt32ValueArrayItemCountOverflow() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_MAX_CNT, int[].class));
    }

    @Test
    public void testBinaryArrayItemCountOverflow() {
        // 0x91 = BC_BINARY, 0x48 = BC_INT32, 0x7FFFFFFF = Integer.MAX_VALUE
        byte[] binaryMaxCnt = {
                (byte) 0x91,
                (byte) 0x48,
                (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertThrows(JSONException.class, () -> JSONB.parseObject(binaryMaxCnt, int[].class));
    }

    @Test
    public void testInt64ValueArrayItemCountOverflow() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_MAX_CNT, long[].class));
    }

    @Test
    public void testInt8ValueArrayItemCountOverflow() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_MAX_CNT, byte[].class));
    }

    @Test
    public void testStringArrayItemCountOverflow() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_MAX_CNT, String[].class));
    }

    @Test
    public void testObjectArrayItemCountOverflow() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_MAX_CNT, Object[].class));
    }

    @Test
    public void testArrayItemCountNegative() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_NEG_CNT, int[].class));
    }

    @Test
    public void testReadAnyItemCountOverflow() {
        assertThrows(JSONException.class, () -> JSONB.parse(ARRAY_LARGE_CNT));
    }

    @Test
    public void testReadArrayItemCountOverflow() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(ARRAY_LARGE_CNT, List.class));
    }

    @Test
    public void testReadObjectItemCountOverflow() {
        assertThrows(JSONException.class, () -> JSONB.parseObject(OBJECT_ARRAY_LARGE_CNT, Map.class));
    }

    @Test
    public void testReadObjectNoTypeItemCountOverflow() {
        // the no-type overload routes through readObject()'s field-value branch
        assertThrows(JSONException.class, () -> JSONB.parseObject(OBJECT_ARRAY_LARGE_CNT));
    }

    @Test
    public void testNestedArrayItemCountOverflow() {
        // outer BC_ARRAY with count 2, first element a small int, second element a
        // nested BC_ARRAY declaring 0x0FFFFFFF items with no bytes left; parseArray
        // routes the nested element through readArray() rather than readAny()
        byte[] buffer = {
                (byte) 0xA4, 0x02,
                0x01,
                (byte) 0xA4, (byte) 0x48, (byte) 0x0F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertThrows(JSONException.class, () -> JSONB.parseArray(buffer));
    }

    @Test
    public void testSkipValueItemCountOverflow() {
        // BC_OBJECT with unknown field "x" whose value declares Integer.MAX_VALUE items
        // and no bytes behind it, so the bean reader skips it through skipValue()'s
        // BC_ARRAY branch and the item loop would run past the end of the input
        byte[] buffer = {
                (byte) 0xA6,
                (byte) 0x4A, (byte) 0x78,
                (byte) 0xA4, (byte) 0x48, (byte) 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        assertThrows(JSONException.class, () -> JSONB.parseObject(buffer, Bean.class));
    }

    @Test
    public void testItemCountMustNotReadBeyondEnd() {
        // a two byte frame declaring eight items, followed in the same buffer by data
        // that belongs to the next frame
        byte[] buffer = {
                (byte) 0xA4, (byte) 0x08,
                41, 42, 43, 44, 45, 46, 47, 10
        };
        assertThrows(JSONException.class, () -> JSONB.parseObject(buffer, 0, 2, int[].class));
    }

    @Test
    public void testItemCountExactlyMatchesRemainingBytes() {
        // BC_ARRAY, count=3 (single-byte int 0x03), three 1-byte items
        byte[] buffer = {
                (byte) 0xA4, 0x03,
                0x01, 0x02, 0x03
        };
        assertArrayEquals(new int[]{1, 2, 3}, JSONB.parseObject(buffer, int[].class));
    }

    @Test
    public void testValidArraysStillParse() {
        int[] ints = {1, 2, 3, 4, 5};
        assertArrayEquals(ints, JSONB.parseObject(JSONB.toBytes(ints), int[].class));

        long[] longs = {Long.MIN_VALUE, 0L, Long.MAX_VALUE};
        assertArrayEquals(longs, JSONB.parseObject(JSONB.toBytes(longs), long[].class));

        String[] strings = {"a", "b", "c"};
        assertArrayEquals(strings, JSONB.parseObject(JSONB.toBytes(strings), String[].class));

        List<Object> list = Arrays.asList(1, "two", 3);
        assertEquals(list, JSONB.parseObject(JSONB.toBytes(list), List.class));

        JSONObject object = JSON.parseObject("{\"a\":[1,2],\"b\":{\"c\":[3]}}");
        assertEquals(object, JSONB.parseObject(JSONB.toBytes(object), Map.class));
    }

    @Test
    public void testLargeValidArrayStillParses() {
        int[] ints = new int[100000];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = i;
        }
        assertArrayEquals(ints, JSONB.parseObject(JSONB.toBytes(ints), int[].class));
    }

    public static class Bean {
        public int id;
    }
}
