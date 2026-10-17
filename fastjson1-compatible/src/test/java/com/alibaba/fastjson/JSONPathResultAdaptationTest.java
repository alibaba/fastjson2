package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class JSONPathResultAdaptationTest {
    private static final String JSON_TEXT = "{\"items\":[{\"id\":1},{\"id\":2}]}";

    @Test
    public void compiledPathDoesNotLeakFastjson2Array() {
        Object root = JSON.parse(JSON_TEXT);

        Object result = JSONPath.compile("$.items[*].id").eval(root);

        JSONArray array = assertInstanceOf(JSONArray.class, result);
        assertEquals("[1,2]", array.toJSONString());
    }

    @Test
    public void readDoesNotLeakFastjson2Array() {
        Object result = JSONPath.read(JSON_TEXT, "$.items[*].id");

        JSONArray array = assertInstanceOf(JSONArray.class, result);
        assertEquals("[1,2]", array.toJSONString());
    }

    @Test
    public void readDoesNotLeakNestedFastjson2Objects() {
        Object result = JSONPath.read(JSON_TEXT, "$.items");

        JSONArray array = assertInstanceOf(JSONArray.class, result);
        assertInstanceOf(JSONObject.class, array.get(0));
        assertEquals(1, array.getJSONObject(0).getIntValue("id"));
    }
}
