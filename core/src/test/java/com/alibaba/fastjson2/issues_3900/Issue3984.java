package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

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

        assertTrue(json.contains("@type"), "JSON should contain @type when WriteClassName is enabled with ValueFilter: " + json);
        assertTrue(json.contains("CustomMap"), "JSON should contain class name: " + json);
    }

    @Test
    public void testWriteClassNameWithoutFilter() {
        CustomMap map = new CustomMap();
        map.put("key1", "value1");

        String json = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);

        assertTrue(json.contains("@type"), "JSON should contain @type when WriteClassName is enabled: " + json);
        assertTrue(json.contains("CustomMap"), "JSON should contain class name: " + json);
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

        // Both outer and inner maps should have @type
        int firstIdx = json.indexOf("@type");
        int lastIdx = json.lastIndexOf("@type");
        assertTrue(firstIdx >= 0, "Should contain @type: " + json);
        assertTrue(firstIdx != lastIdx, "Should contain @type for both outer and inner maps: " + json);
    }
}
