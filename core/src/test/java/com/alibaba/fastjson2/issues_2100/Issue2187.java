package com.alibaba.fastjson2.issues_2100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Issue2187 {
    @Test
    public void test() throws Exception {
        Bean bean = new Bean();
        bean.items = new ArrayList<>();
        bean.items.add(new Item());
        JSONObject jsonObject = (JSONObject) JSON.toJSON(bean);
        JSONArray items = (JSONArray) jsonObject.get("items");
        assertEquals(1, items.size());
    }

    @Data
    public static class Bean {
        List<Item> items;
    }

    public static class Item {
    }

    @Test
    public void test1() throws Exception {
        JSONObject object = new JSONObject();
        object.put("ref", object);
        assertSame(object, JSON.toJSON(object));
    }

    @Test
    public void test2() throws Exception {
        JSONArray array = new JSONArray();
        array.add(array);
        assertSame(array, JSON.toJSON(array));
    }

    @Test
    public void test3() throws Exception {
        Bean3 bean = new Bean3();
        bean.value = bean;
        JSONObject json = (JSONObject) JSON.toJSON(bean);
        assertSame(json, json.get("value"));
    }

    public static class Bean3 {
        public Bean3 value;
    }

    @Test
    public void test4() throws Exception {
        Bean4 bean = new Bean4();
        bean.values = Arrays.asList(bean);
        JSONObject json = (JSONObject) JSON.toJSON(bean);
        assertSame(json, json.getJSONArray("values").get(0));
    }

    public static class Bean4 {
        public List<Bean4> values;
    }
}
