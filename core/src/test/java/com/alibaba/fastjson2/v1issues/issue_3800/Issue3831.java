package com.alibaba.fastjson2.v1issues.issue_3800;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3831 {
    @Test
    public void test_for_issue3831() {
        Map<String, Object> longType = new LinkedHashMap<>();
        longType.put("type", "long");

        Map<String, Object> textType = new LinkedHashMap<>();
        textType.put("type", "text");
        textType.put("analyzer", "standard");

        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("type", "keyword");
        raw.put("doc_values", "false");

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("raw", raw);

        Map<String, Object> rawLongType = new LinkedHashMap<>();
        rawLongType.put("type", "long");
        rawLongType.put("fields", fields);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("id", longType);
        properties.put("name", textType);
        properties.put("cityId", longType);
        properties.put("categoryId", rawLongType);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("properties", properties);

        assertEquals(com.alibaba.fastjson2.JSON.toJSONString(result), new com.google.gson.Gson().toJson(result));
    }
}
