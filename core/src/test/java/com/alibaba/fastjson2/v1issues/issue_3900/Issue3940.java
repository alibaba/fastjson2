package com.alibaba.fastjson2.v1issues.issue_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3940 {
    JSONObject jo;

    @BeforeEach
    public void init() {
        jo = new JSONObject();
    }

    @Test
    public void testInteger() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(1, 2);
        assertEquals("{1:2}", JSON.toJSONString(map));
        assertEquals("{\"1\":2}", JSON.toJSONString(map, JSONWriter.Feature.WriteNonStringKeyAsString));
    }

    @Test
    public void testFloat() {
        Map<Float, Integer> map = new HashMap<Float, Integer>();
        map.put(1.23F, 2);
        assertEquals("{1.23:2}", JSON.toJSONString(map));
        assertEquals("{\"1.23\":2}", JSON.toJSONString(map, JSONWriter.Feature.WriteNonStringKeyAsString));
    }

    @Test
    public void testDouble() {
        Map<Double, Integer> map = new HashMap<Double, Integer>();
        map.put(1.23, 2);
        assertEquals("{1.23:2}", JSON.toJSONString(map));
        assertEquals("{\"1.23\":2}", JSON.toJSONString(map, JSONWriter.Feature.WriteNonStringKeyAsString));
    }
}
