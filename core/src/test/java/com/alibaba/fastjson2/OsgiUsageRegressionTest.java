package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the OSGi usage scenarios:
 * 1. toJSONString with PrettyFormat on JSONObject/JSONArray must not recurse infinitely
 *    (ObjectWriterBaseModule inline writers must serialize content directly)
 * 2. Arrays containing null elements must parse (readNull must consume trailing comma)
 * 3. readBoolValue must consume trailing comma
 */
public class OsgiUsageRegressionTest {
    @Test
    public void prettyFormatJSONObject() {
        JSONObject obj = JSON.parseObject("{\"name\":\"fastjson2\",\"version\":2,\"enable\":true,\"total\":123,\"gp\":[1,2,3],\"child\":{\"x\":1}}");
        String pretty = JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
        assertTrue(pretty.contains("\n"));
        assertEquals(obj, JSON.parseObject(pretty));
    }

    @Test
    public void prettyFormatJSONArray() {
        JSONArray arr = JSON.parseArray("[1,2,3]");
        String pretty = JSON.toJSONString(arr, JSONWriter.Feature.PrettyFormat);
        assertTrue(pretty.contains("\n"));
        assertEquals("[1,2,3]", JSON.parseArray(pretty).toJSONString());
    }

    @Test
    public void prettyFormatNestedNullEmpty() {
        JSONObject nested = new JSONObject();
        nested.put("a", JSON.parseObject("{\"b\":[true,false,null,\"s\",1.5]}"));
        nested.put("empty", new JSONObject());
        nested.put("emptya", new JSONArray());
        String pretty = JSON.toJSONString(nested, JSONWriter.Feature.PrettyFormat);
        JSONObject back = JSON.parseObject(pretty);
        assertEquals(1.5D, back.getJSONObject("a").getJSONArray("b").getDouble(4));
        assertNull(back.getJSONObject("a").getJSONArray("b").get(2));
        assertTrue(back.getJSONObject("empty").isEmpty());
        assertTrue(back.getJSONArray("emptya").isEmpty());
    }

    @Test
    public void jsonObjectInstancePretty() {
        JSONObject obj = new JSONObject();
        obj.put("code", 0);
        JSONArray list = new JSONArray();
        list.add("a");
        list.add("b");
        obj.put("list", list);
        String pretty = obj.toJSONString(JSONWriter.Feature.PrettyFormat);
        assertTrue(pretty.contains("\n"));
        assertEquals(0, JSON.parseObject(pretty).getIntValue("code"));
    }

    @Test
    public void parseArrayWithNull() {
        assertEquals("[null,\"s\"]", JSON.parseArray("[null,\"s\"]").toJSONString());
        assertEquals("[\"s\",null]", JSON.parseArray("[\"s\",null]").toJSONString());
        assertEquals("[null]", JSON.parseArray("[null]").toJSONString());
        assertEquals("[1,null,2]", JSON.parseArray("[1,null,2]").toJSONString());
        assertEquals("[true,false,null,\"s\",1.5]", JSON.parseArray("[true,false,null,\"s\",1.5]").toJSONString());
        assertNull(JSON.parseArray("[null,\"s\"]").get(0));
    }

    @Test
    public void parseObjectWithNullInArray() {
        JSONObject obj = JSON.parseObject("{\"b\":[true,false,null,\"s\",1.5]}");
        JSONArray b = obj.getJSONArray("b");
        assertNull(b.get(2));
        assertEquals(1.5D, b.getDouble(4));
    }

    @Test
    public void readBoolValueInArray() {
        JSONArray arr = JSON.parseArray("[true,false]");
        assertTrue(arr.getBooleanValue(0));
        assertFalse(arr.getBooleanValue(1));
    }

    @Test
    public void nestedRoundtripPretty() {
        JSONObject big = JSON.parseObject("{\"data\":{\"items\":[{\"id\":1,\"name\":\"n1\"},{\"id\":2,\"name\":\"n2\"}],\"total\":2},\"code\":200}");
        String bigJson = JSON.toJSONString(big, JSONWriter.Feature.PrettyFormat);
        JSONObject bigBack = JSON.parseObject(bigJson);
        assertEquals("n2", bigBack.getJSONObject("data").getJSONArray("items").getJSONObject(1).getString("name"));
    }

    @Test
    public void writeNullsEmptyObject() {
        JSONObject obj = new JSONObject();
        assertEquals("{}", JSON.toJSONString(obj));
        assertEquals("{}", obj.toJSONString());
        JSONArray arr = new JSONArray();
        assertEquals("[]", JSON.toJSONString(arr));
        assertEquals("[]", arr.toJSONString());
    }
}
