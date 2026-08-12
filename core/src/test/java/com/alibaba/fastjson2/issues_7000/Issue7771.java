package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7771 {
    @Test
    public void containsOnJsonStringMatchesEval() {
        String json = "{\"status\": 200}";

        assertEquals(200, JSONPath.eval(json, "$.status"));
        assertTrue(JSONPath.contains(json, "$.status"));
        assertFalse(JSONPath.contains(json, "$.missing"));
    }

    @Test
    public void containsOnJsonStringNullValueStillPresent() {
        String json = "{\"status\": null}";

        assertTrue(JSONPath.contains(json, "$.status"));
        assertFalse(JSONPath.contains(json, "$.missing"));
    }

    @Test
    public void containsOnParsedObjectUnchanged() {
        JSONObject object = JSON.parseObject("{\"status\": 200}");
        assertTrue(JSONPath.contains(object, "$.status"));
        assertFalse(JSONPath.contains(object, "$.missing"));
    }
}
