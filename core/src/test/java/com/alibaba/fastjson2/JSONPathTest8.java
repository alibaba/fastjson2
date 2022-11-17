package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}
