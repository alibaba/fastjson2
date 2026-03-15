package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.filter.ValueFilter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3984 {
    static final String CUSTOM_MAP_TYPE = CustomMap.class.getName();

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

        JSONObject parsed = JSON.parseObject(json);
        assertEquals(CUSTOM_MAP_TYPE, parsed.getString("@type"));
        assertEquals("value1", parsed.getString("key1"));
    }

    @Test
    public void testWriteClassNameWithoutFilter() {
        CustomMap map = new CustomMap();
        map.put("key1", "value1");

        String json = JSON.toJSONString(map, JSONWriter.Feature.WriteClassName);

        JSONObject parsed = JSON.parseObject(json);
        assertEquals(CUSTOM_MAP_TYPE, parsed.getString("@type"));
        assertEquals("value1", parsed.getString("key1"));
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

        JSONObject parsed = JSON.parseObject(json);
        assertEquals(CUSTOM_MAP_TYPE, parsed.getString("@type"));
        assertEquals("value1", parsed.getString("key1"));

        JSONObject nestedParsed = parsed.getJSONObject("nested");
        assertEquals(CUSTOM_MAP_TYPE, nestedParsed.getString("@type"));
        assertEquals(123, nestedParsed.getIntValue("msg"));
    }
}
