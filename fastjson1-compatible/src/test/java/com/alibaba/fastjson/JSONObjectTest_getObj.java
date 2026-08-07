package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JSONObjectTest_getObj {
    @Test
    public void test_get_empty() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", "");
        assertEquals("", obj.get("value"));
        assertNull(obj.getObject("value", Model.class));
        assertNull(obj.getObject("value", new TypeReference<Model>() {
        }));
    }

    @Test
    public void test_get_null() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", "null");
        assertEquals("null", obj.get("value"));
        assertNull(obj.getObject("value", Model.class));
        assertNull(obj.getObject("value", new TypeReference<Model>() {
        }));
    }

    @Test
    public void test_get_obj() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", new HashMap());
        assertEquals(new JSONObject(), obj.getObject("value", JSONObject.class));
    }

    @Test
    public void test_get_obj2() throws Exception {
        List<JSONObject> json = JSON.parseArray("[{\"values\":[{}]}]", JSONObject.class);

        for (JSONObject obj : json) {
            Object values = obj.getObject("values", new TypeReference<List<JSONObject>>() {
            });
        }
    }

    public static class Model {
    }
}
