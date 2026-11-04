package com.alibaba.fastjson2.issues_7900;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A truncated JSONB buffer must be reported as {@link JSONException}. It used to escape as
 * {@link ArrayIndexOutOfBoundsException}, which is not a {@code JSONException} subclass, so a
 * caller following the documented contract did not catch it.
 */
public class Issue7946 {
    private static void assertTruncated(String expected, byte[] bytes) {
        assertTruncated(expected, () -> JSONB.parse(bytes));
    }

    private static void assertTruncated(String expected, Runnable call) {
        JSONException e = assertThrows(JSONException.class, call::run);
        assertTrue(
                e.getMessage() != null && e.getMessage().contains(expected),
                "expected message containing \"" + expected + "\", but was: " + e.getMessage());
    }

    @Test
    public void emptyInput() {
        assertTruncated("truncated", new byte[0]);

        assertThrows(JSONException.class, () -> JSONB.parseObject(new byte[0], Map.class));
        assertThrows(JSONException.class, () -> JSONB.parseArray(new byte[0]));
        assertThrows(JSONException.class, () -> JSONB.parseObject(new byte[0], Object.class));
    }

    @Test
    public void truncatedObject() {
        byte[] full = JSONB.toBytes(Collections.singletonMap("a", 1));
        assertEquals(5, full.length);

        for (int i = 0; i < full.length; i++) {
            byte[] truncated = Arrays.copyOf(full, i);
            assertThrows(JSONException.class, () -> JSONB.parse(truncated), "length " + i);
        }

        // truncated right after the BC_OBJECT tag, and missing the BC_OBJECT_END
        assertTruncated("truncated object", Arrays.copyOf(full, 1));
        assertTruncated("truncated object", Arrays.copyOf(full, 4));
    }

    @Test
    public void truncatedArray() {
        byte[] full = JSONB.toBytes(Arrays.asList(1, 2, 3));

        for (int i = 0; i < full.length; i++) {
            byte[] truncated = Arrays.copyOf(full, i);
            assertThrows(JSONException.class, () -> JSONB.parseArray(truncated), "length " + i);
        }

        assertTruncated("truncated array", () -> JSONB.parseArray(Arrays.copyOf(full, 1)));
    }

    @Test
    public void truncatedNestedObject() {
        byte[] full = JSONB.toBytes(Collections.singletonMap("a", Collections.singletonMap("b", 1)));
        assertEquals(9, full.length);

        for (int i = 0; i < full.length; i++) {
            byte[] truncated = Arrays.copyOf(full, i);
            assertThrows(
                    JSONException.class,
                    () -> JSONB.parseObject(truncated, Map.class),
                    "length " + i);
        }

        assertTruncated("truncated object", Arrays.copyOf(full, 7));
    }

    @Test
    public void truncatedStringValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "abcdef");
        byte[] full = JSONB.toBytes(map);

        for (int i = 0; i < full.length; i++) {
            byte[] truncated = Arrays.copyOf(full, i);
            assertThrows(JSONException.class, () -> JSONB.parse(truncated), "length " + i);
        }
    }

    @Test
    public void truncatedUtf16String() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "中文值测试");
        byte[] full = JSONB.toBytes(map);

        for (int i = 0; i < full.length; i++) {
            byte[] truncated = Arrays.copyOf(full, i);
            assertThrows(JSONException.class, () -> JSONB.parse(truncated), "length " + i);
        }
    }

    @Test
    public void truncatedValuePayload() {
        Object[] values = {
                new BigDecimal("-12345.6789"),
                new BigInteger("123456789012345678901234567890"),
                1234567890123456789L,
                3.14159265358979d,
                new byte[]{1, 2, 3, 4, 5}
        };

        for (Object value : values) {
            Map<String, Object> map = new HashMap<>();
            map.put("v", value);
            byte[] full = JSONB.toBytes(map);
            String label = value.getClass().getSimpleName();

            for (int i = 0; i < full.length; i++) {
                byte[] truncated = Arrays.copyOf(full, i);
                assertThrows(JSONException.class, () -> JSONB.parse(truncated), label + " length " + i);
            }
        }
    }

    @Test
    public void fullInputStillParses() {
        Map<String, Object> map = Collections.singletonMap("a", 1);
        assertEquals(map, JSONB.parse(JSONB.toBytes(map)));
        assertEquals(map, JSONB.parseObject(JSONB.toBytes(map), Map.class));

        List<Integer> list = Arrays.asList(1, 2, 3);
        assertEquals(list, JSONB.parseArray(JSONB.toBytes(list)));

        Map<String, Object> nested = Collections.singletonMap("a", Collections.singletonMap("b", 1));
        assertEquals(nested, JSONB.parseObject(JSONB.toBytes(nested), Map.class));

        assertEquals(new HashMap<>(), JSONB.parse(JSONB.toBytes(new HashMap<>())));
        assertEquals(new ArrayList<>(), JSONB.parseArray(JSONB.toBytes(new ArrayList<>())));
    }
}
