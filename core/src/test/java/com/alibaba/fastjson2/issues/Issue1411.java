package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class Issue1411 {
    @Test
    public void test() {
        String text = "{\"@type\":\"java.util.HashMap\"}";
        Object result = JSON.parse(text, JSONReader.Feature.SupportAutoType);
        assertInstanceOf(HashMap.class, result);
        assertEquals("{}", JSON.toJSONString(result));

        assertEquals(
                "java.util.HashMap",
                ((JSONObject) JSON.parse(text))
                        .get("@type")
        );
    }

    @Test
    public void test2() {
        String text = "{\"@type\":\"java.util.TreeMap\"}";
        Object result = JSON.parse(text, JSONReader.Feature.SupportAutoType);
        assertInstanceOf(TreeMap.class, result);
        assertEquals("{}", JSON.toJSONString(result));

        assertEquals(
                "java.util.TreeMap",
                ((JSONObject) JSON.parse(text))
                        .get("@type")
        );
    }

    @Test
    public void test3() {
        String text = "{\"@type\":\"com.alibaba.fastjson2.issues.Issue1411\"}";
        Object result = JSON.parse(text, JSONReader.Feature.SupportAutoType);
        assertInstanceOf(Issue1411.class, result);
        assertEquals("{}", JSON.toJSONString(result));

        assertEquals(
                "com.alibaba.fastjson2.issues.Issue1411",
                ((JSONObject) JSON.parse(text))
                        .get("@type")
        );
    }

    @Test
    public void test4() {
        String text = "{\"@type\":\"java.util.concurrent.ConcurrentHashMap\"}";
        Object result = JSON.parse(text, JSONReader.Feature.SupportAutoType);
        assertInstanceOf(ConcurrentHashMap.class, result);
        assertEquals("{}", JSON.toJSONString(result));

        assertEquals(
                "java.util.concurrent.ConcurrentHashMap",
                ((JSONObject) JSON.parse(text))
                        .get("@type")
        );
    }

    @Test
    public void test5() {
        String text = "{\"@type\":\"java.util.concurrent.ConcurrentHashMap\",\"id\":2,\"name\":\"fastjson\"}";
        Object result = JSON.parse(text, JSONReader.Feature.SupportAutoType);
        assertInstanceOf(ConcurrentHashMap.class, result);
        assertEquals("{\"name\":\"fastjson\",\"id\":2}", JSON.toJSONString(result));

        JSONObject jsonObject = (JSONObject) JSON.parse(text);
        assertEquals("java.util.concurrent.ConcurrentHashMap", jsonObject.get("@type"));
        assertEquals(2, jsonObject.get("id"));
        assertEquals("fastjson", jsonObject.get("name"));
    }
}
