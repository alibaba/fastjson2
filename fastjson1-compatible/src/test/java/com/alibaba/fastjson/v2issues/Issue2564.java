package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2564 {
    @Test
    public void test_mutated_issue() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "value");

        Object jsonObjectJavaObj1 = jsonObject.to(JSONObject.class);
        Object jsonObjectJavaObj2 = jsonObject.to(Map.class);
        Object jsonObjectJavaObj3 = jsonObject.to(JSON.class);
        Object jsonObjectJavaObj4 = JSON.toJSON(jsonObject);

        assertEquals(jsonObject, jsonObjectJavaObj1);
        assertEquals(jsonObject, jsonObjectJavaObj2);
        assertEquals(jsonObject, jsonObjectJavaObj3);
        assertEquals(jsonObject, jsonObjectJavaObj4);
    }

    @Test
    public void test_mutated_issue_array() {
        JSONArray jsonObject = new JSONArray();
        jsonObject.add("value");

        Object jsonObjectJavaObj1 = jsonObject.to(JSONArray.class);
        Object jsonObjectJavaObj2 = jsonObject.to(Collection.class);
        Object jsonObjectJavaObj3 = jsonObject.to(JSON.class);
        Object jsonObjectJavaObj4 = JSON.toJSON(jsonObject);

        assertEquals(jsonObject, jsonObjectJavaObj1);
        assertEquals(jsonObject, jsonObjectJavaObj2);
        assertEquals(jsonObject, jsonObjectJavaObj3);
        assertEquals(jsonObject, jsonObjectJavaObj4);
    }

    @Test
    public void test_mutated_issue1() {
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("key", "value");

        Object jsonObjectJavaObj1 = jsonObject.toJavaObject(com.alibaba.fastjson.JSONObject.class);
        Object jsonObjectJavaObj2 = jsonObject.toJavaObject(Map.class);
        Object jsonObjectJavaObj3 = jsonObject.toJavaObject(com.alibaba.fastjson.JSON.class);

        assertEquals(jsonObject, jsonObjectJavaObj1);
        assertEquals(jsonObject, jsonObjectJavaObj2);
        assertEquals(jsonObject, jsonObjectJavaObj3);
    }
}
