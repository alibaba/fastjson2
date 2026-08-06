package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue7765 {
    @Test
    public void testGetJSONArrayFromString() {
        String json = "{\"items\":\"[{\\\"name\\\":\\\"alpha\\\"},[1,2]]\"}";
        JSONArray items = JSON.parseObject(json).getJSONArray("items");

        Object[] elements = items.toArray();
        assertEquals(JSONObject.class, elements[0].getClass());
        assertEquals(JSONArray.class, elements[1].getClass());
        assertEquals(JSONObject.class, items.iterator().next().getClass());
    }

    @Test
    public void testGetJSONObjectFromString() {
        String json = "{\"item\":\"{\\\"inner\\\":{\\\"name\\\":\\\"alpha\\\"},\\\"list\\\":[1]}\"}";
        JSONObject item = JSON.parseObject(json).getJSONObject("item");

        for (Map.Entry<String, Object> entry : item.entrySet()) {
            Class<?> expected = "inner".equals(entry.getKey()) ? JSONObject.class : JSONArray.class;
            assertEquals(expected, entry.getValue().getClass());
        }
    }

    @Test
    public void testArrayGetJSONArrayFromString() {
        JSONArray array = JSON.parseArray("[\"[{\\\"name\\\":\\\"alpha\\\"}]\"]");

        assertEquals(JSONObject.class, array.getJSONArray(0).toArray()[0].getClass());
    }

    @Test
    public void testArrayGetJSONObjectFromString() {
        JSONArray array = JSON.parseArray("[\"{\\\"inner\\\":{\\\"name\\\":\\\"alpha\\\"}}\"]");

        assertEquals(JSONObject.class, array.getJSONObject(0).values().iterator().next().getClass());
    }

    @Test
    public void testGetJSONArrayFromArray() {
        String json = "{\"items\":[{\"name\":\"alpha\"},{\"name\":\"beta\"}]}";
        JSONArray items = JSON.parseObject(json).getJSONArray("items");

        assertEquals(JSONObject.class, items.toArray()[0].getClass());
    }

    @Test
    public void testGetObjectByTypeReference() {
        JSONObject root = JSON.parseObject("{\"v\":{\"inner\":{\"name\":\"alpha\"},\"list\":[1]}}");

        Map<String, Object> value = root.getObject("v", new TypeReference<Map<String, Object>>() {});
        assertEquals(JSONObject.class, value.get("inner").getClass());
        assertEquals(JSONArray.class, value.get("list").getClass());
    }

    @Test
    public void testArrayGetObjectByBeanClass() {
        JSONArray array = JSON.parseArray("[{\"inner\":{\"name\":\"alpha\"},\"list\":[{\"name\":\"beta\"}]}]");

        Bean bean = array.getObject(0, Bean.class);
        assertEquals(JSONObject.class, bean.inner.getClass());
        assertEquals(JSONObject.class, bean.list.get(0).getClass());
    }

    public static class Bean {
        public Object inner;
        public List<Object> list;
    }
}
