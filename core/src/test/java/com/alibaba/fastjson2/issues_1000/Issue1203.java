package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1203 {
    @Test
    public void test() {
        JSONArray array = new JSONArray();
        array.add("a");
        JSONObject object = new JSONObject();
        object.put("array", array);
        Yaml yaml = new Yaml();
        JSONObject json = yaml.loadAs(
                new StringReader(object.toString()),
                JSONObject.class
        );
        JSONArray res = json.getJSONArray("array");
        res.add("b");
        assertEquals(
                "[\"a\",\"b\"]",
                json.getJSONArray("array").toString()
        );
    }

    @Test
    public void test1() {
        JSONArray array = new JSONArray();
        ArrayList list = new ArrayList();
        list.add("a");
        array.add(list);
        JSONArray res = array.getJSONArray(0);
        res.add("b");
        assertEquals(
                "[\"a\",\"b\"]",
                array.getJSONArray(0).toString()
        );
    }

    @Test
    public void test2() {
        JSONArray array = new JSONArray();
        Map map = new HashMap();
        map.put("a", 1);
        array.add(map);
        JSONObject res = array.getJSONObject(0);
        res.put("b", 2);
        assertEquals(
                "{\"a\":1,\"b\":2}",
                array.getJSONObject(0).toString()
        );
    }

    @Test
    public void test3() {
        JSONObject root = new JSONObject();
        Map map = new HashMap();
        map.put("a", 1);
        root.put("items", map);
        JSONObject res = root.getJSONObject("items");
        res.put("b", 2);
        assertEquals(
                "{\"a\":1,\"b\":2}",
                root.getJSONObject("items").toString()
        );
    }
}
