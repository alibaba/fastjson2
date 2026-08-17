package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void containsOnJsonNullLiteralIsFalse() {
        assertFalse(JSONPath.contains("null", "$.status"));
        assertFalse(JSONPath.contains("null", "$"));
        assertFalse(JSONPath.contains((String) null, "$.status"));
        assertFalse(JSONPath.contains("", "$.status"));
    }

    @Test
    public void containsOnRootPathMatchesEval() {
        assertTrue(JSONPath.contains("{\"status\":200}", "$"));
        assertTrue(JSONPath.contains("123", "$"));
        assertTrue(JSONPath.contains("[1,2]", "$"));
        assertEquals(JSON.parse("{\"status\":200}"), JSONPath.eval("{\"status\":200}", "$"));
        assertTrue(JSONPath.contains(JSON.parse("{\"a\":1}"), "$"));
    }

    @Test
    public void containsOnArrayRoot() {
        assertTrue(JSONPath.contains("[1,2,3]", "$[1]"));
        assertFalse(JSONPath.contains("[1,2,3]", "$[5]"));
        assertFalse(JSONPath.contains("[null]", "$.status"));
        assertFalse(JSONPath.contains("[null,{\"status\":1}]", "$.status"));
        assertTrue(JSONPath.contains("[{\"status\":1},null]", "$.status"));
        assertTrue(JSONPath.contains("[null]", "$[0]"));
        assertTrue(JSONPath.contains("{\"a\":[null]}", "$.a[0]"));
        assertTrue(JSONPath.contains("[[]]", "$[0]"));
    }

    @Test
    public void containsTreatsPresentNullAsPresent() {
        assertTrue(JSONPath.contains("{\"a\":[{\"status\":null}]}", "$.a.status"));
        assertTrue(JSONPath.contains("{\"a\":{\"status\":null}}", "$.a.status"));
        assertTrue(JSONPath.contains("[{\"status\":null}]", "$.status"));
        assertTrue(JSONPath.contains("{\"a\":[{\"status\":null}]}", "$.a[0].status"));
    }

    @Test
    public void containsOnEmptyMatchSetIsFalse() {
        assertFalse(JSONPath.contains("{}", "$.*"));
        assertFalse(JSONPath.contains("{}", "$..a"));
        assertFalse(JSONPath.contains("[1,2]", "$[5,6]"));
        assertFalse(JSONPath.contains("[1,2]", "$[?(@>9)]"));
        assertFalse(JSONPath.contains("[1,2]", "$[5:6]"));
        assertFalse(JSONPath.contains("[1,2]", "$[0:0]"));
        assertFalse(JSONPath.contains("{}", "$['x','y']"));
    }

    @Test
    public void containsOnMalformedJsonThrows() {
        assertThrows(JSONException.class, () -> JSONPath.contains("{invalid json", "$.status"));
    }

    @Test
    public void containsOnRefCycleDoesNotOverflow() {
        assertTrue(JSONPath.contains("{\"a\":{\"$ref\":\"$\"}}", "$..a"));
        assertTrue(JSONPath.contains("{\"x\":{\"y\":{\"$ref\":\"$\"}}}", "$..x"));
    }
}
