package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue728 {
    @Test
    public void test() {
        String a = "{\"test\":\"123465\"}";
        LinkedHashMap linkedHashMap = JSON.toJavaObject(a, LinkedHashMap.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", linkedHashMap);
        Bean bean = jsonObject.toJavaObject(Bean.class);
        assertNotNull(bean.data);
        Object test = bean.data.get("test");
        assertEquals("123465", test);
    }

    public static class Bean {
        public JSONObject data;
    }

    @Test
    public void test1() {
        JSONObject jsonObject = new JSONObject();
        ArrayList list = new ArrayList();
        list.add("123465");
        jsonObject.put("data", list);
        Bean1 bean = jsonObject.to(Bean1.class);
        ArrayList list1 = bean.data;
        assertEquals(1, list1.size());
        assertEquals(list.get(0), list1.get(0));
    }

    public static class Bean1 {
        public JSONArray data;
    }

    @Test
    public void test2() {
        String a = "{\"test\":\"123465\"}";
        LinkedHashMap linkedHashMap = JSON.toJavaObject(a, LinkedHashMap.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", linkedHashMap);
        Bean2 bean = jsonObject.toJavaObject(Bean2.class);
        assertNotNull(bean.data);
        Object test = bean.data.get("test");
        assertEquals("123465", test);
    }

    public static class Bean2 {
        public com.alibaba.fastjson.JSONObject data;
    }
}
