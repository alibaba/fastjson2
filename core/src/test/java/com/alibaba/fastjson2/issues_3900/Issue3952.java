package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3952 {
    @Test
    public void testLongKeyQuoted() {
        Map<Long, String> map = new HashMap<>();
        map.put(123456789012345678L, "value");

        String json = JSON.toJSONString(map);
        assertEquals("{\"123456789012345678\":\"value\"}", json);
    }

    @Test
    public void testLongKeyMinMaxQuoted() {
        Map<Long, String> map = new java.util.LinkedHashMap<>();
        map.put(Long.MIN_VALUE, "min");
        map.put(Long.MAX_VALUE, "max");

        String json = JSON.toJSONString(map);
        assertEquals("{\"-9223372036854775808\":\"min\",\"9223372036854775807\":\"max\"}", json);
    }

    @Test
    public void testLongKeyNegativeAndZeroQuoted() {
        Map<Long, String> map = new java.util.LinkedHashMap<>();
        map.put(-1L, "neg");
        map.put(0L, "zero");

        String json = JSON.toJSONString(map);
        assertEquals("{\"-1\":\"neg\",\"0\":\"zero\"}", json);
    }

    @Test
    public void testNestedLongKeyQuoted() {
        Map<Long, String> inner = new java.util.LinkedHashMap<>();
        inner.put(7L, "v");
        Map<String, Object> outer = new java.util.LinkedHashMap<>();
        outer.put("inner", inner);

        String json = JSON.toJSONString(outer);
        assertEquals("{\"inner\":{\"7\":\"v\"}}", json);
    }
}
