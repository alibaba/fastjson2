package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue3984 {
    public static class CustomMap extends HashMap<String, Object> {
    }

    @Test
    public void testWriteClassNameWithValueFilter() {
        CustomMap map = new CustomMap();
        map.put("key1", "value1");

        ValueFilter valueFilter = (object, name, value) -> value;

        String json = JSON.toJSONString(map,
                new ValueFilter[]{valueFilter},
                JSONWriter.Feature.WriteClassName);

        String expectedType = "\"@type\":\"" + CustomMap.class.getName() + "\"";
        assertTrue(json.contains(expectedType),
                "JSON should contain " + expectedType + ", actual: " + json);
    }

    @Test
    public void testWriteClassNameWithoutFilter() {
        CustomMap map = new CustomMap();
        map.put("key1", "value1");

        String json = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);

        String expectedType = "\"@type\":\"" + CustomMap.class.getName() + "\"";
        assertTrue(json.contains(expectedType),
                "JSON should contain " + expectedType + ", actual: " + json);
    }

    @Test
    public void testNestedMapWithValueFilter() {
        CustomMap inner = new CustomMap();
        inner.put("msg", 123);

        CustomMap outer = new CustomMap();
        outer.put("key1", "value1");
        outer.put("nested", inner);

        ValueFilter valueFilter = (object, name, value) -> value;

        String json = JSON.toJSONString(outer,
                new ValueFilter[]{valueFilter},
                JSONWriter.Feature.WriteClassName);

        String typeToken = "\"@type\":\"" + CustomMap.class.getName() + "\"";
        int count = 0;
        int idx = 0;
        while ((idx = json.indexOf(typeToken, idx)) != -1) {
            count++;
            idx += typeToken.length();
        }
        assertEquals(2, count,
                "Should contain exactly 2 @type entries for outer and inner maps: " + json);
    }
}
