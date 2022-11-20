package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JSONPathTest8 {
    @Test
    public void lower() {
        JSONPath jsonPath = JSONPath.of("$.lower()");
        assertEquals("abc", jsonPath.eval("ABC"));
        assertEquals("1", jsonPath.eval(1));
        assertNull(jsonPath.eval(null));
    }

    @Test
    public void upper() {
        JSONPath jsonPath = JSONPath.of("$.upper()");
        assertEquals("ABC", jsonPath.eval("ABC"));
        assertEquals("1", jsonPath.eval(1));
        assertNull(jsonPath.eval(null));
    }

    @Test
    public void trim() {
        JSONPath jsonPath = JSONPath.of("$.trim()");
        assertEquals("ABC", jsonPath.eval(" ABC "));
        assertEquals("1", jsonPath.eval(1));
        assertNull(jsonPath.eval(null));
    }

    @Test
    public void testName() {
        assertNull(
                JSONPath.of("$.value", BigDecimal.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", Integer.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", Long.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", String.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", Float.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", Double.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", Boolean.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", Short.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", Byte.class)
                        .extract("{}")
        );
        assertNull(
                JSONPath.of("$.value", UUID.class)
                        .extract("{}")
        );
    }

    @Test
    public void singleIndex() {
        JSONPath path = JSONPath.of("$[-1]");
        assertEquals(
                4,
                path.eval(
                        JSONArray.of(1, 2, 3, 4)
                )
        );
        assertEquals(
                4,
                path.eval(
                        new Integer[]{1, 2, 3, 4}
                )
        );
        assertNull(path.eval(null));

        assertEquals(
                4,
                path.extract("[1,2,3,4]")
        );
    }

    @Test
    public void singleIndex1() {
        JSONPath path = JSONPath.of("$[0]");

        assertEquals(
                4,
                path.eval(
                        Collections.singleton(4)
                )
        );

        assertEquals(
                1,
                path.extract("[1,2,3,4]")
        );
        assertNull(path.extract("null"));
        assertThrows(JSONException.class, () -> path.extract("{}"));
    }

    @Test
    public void singleIndex2() {
        JSONPath path = JSONPath.of("$[1]");

        assertEquals(
                2,
                path.extract("[1,2,3,4]")
        );
        assertNull(path.eval(null));
    }
}
