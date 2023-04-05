package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathSetTest1 {
    @Test
    public void test() {
        JSONArray array = JSON.parseArray("[0,1,2,3]");
        JSONPath.set(array, "$[?(@ == 1)]", 9);
        assertEquals("[0,9,2,3]", array.toJSONString());
    }

    @Test
    public void test_str() {
        assertEquals("[0,9,2,3]", JSONPath.set("[0,1,2,3]", "$[?(@ == 1)]", 9));
    }

    @Test
    public void test1() {
        JSONObject object = JSON.parseObject("{'store':{'book':['x0','x1']}}");
        JSONPath.set(object, "$.store.book[0]", "a");
        assertEquals("{\"store\":{\"book\":[\"a\",\"x1\"]}}", object.toJSONString());
    }

    @Test
    public void test1_str() {
        assertEquals("{\"store\":{\"book\":[\"a\",\"x1\"]}}", JSONPath.set("{'store':{'book':['x0','x1']}}", "$.store.book[0]", "a"));
    }

    @Test
    public void test2() {
        JSONObject object = JSON.parseObject("{'store':{'book':['x0','x1','x2','x3']}}");
        JSONPath.set(object, "$.store.book[0:2]", "a");
        assertEquals("{\"store\":{\"book\":[\"a\",\"a\",\"x2\",\"x3\"]}}", object.toJSONString());
    }

    @Test
    public void test2_str() {
        assertEquals("{\"store\":{\"book\":[\"a\",\"a\",\"x2\",\"x3\"]}}",
                JSONPath.set("{'store':{'book':['x0','x1','x2','x3']}}", "$.store.book[0:2]", "a"));
    }

    @Test
    public void test3() {
        JSONObject root = JSONObject.of("values", JSONArray.of(1, 2, 3));
        JSONPath.set(root, "$.values[*]", 100);
        assertEquals("[100,100,100]", root.get("values").toString());
    }

    @Test
    public void test4() {
        JSONObject root = JSONObject.of("values", JSONObject.of("a", 1, "b2", 2, "c", 3));
        JSONPath.set(root, "$.values[*]", 100);
        assertEquals("{\"a\":100,\"b2\":100,\"c\":100}", root.get("values").toString());
    }

    @Test
    public void test5() {
        JSONObject root = JSONObject.of("values", JSONArray.of(1, 2, 3));
        JSONPath.setCallback(root, "$.values[*]", v -> ((Integer) v).intValue() + 100);
        assertEquals("[101,102,103]", root.get("values").toString());
    }

    @Test
    public void test6() {
        JSONObject root = JSONObject.of("values", JSONObject.of("a", 1, "b2", 2, "c", 3));
        JSONPath.setCallback(root, "$.values[*]", v -> ((Integer) v).intValue() + 100);
        assertEquals("{\"a\":101,\"b2\":102,\"c\":103}", root.get("values").toString());
    }

    @Test
    public void test7() {
        JSONObject root = JSONObject.of("values", new int[] {1, 2, 3});
        JSONPath.setCallback(root, "$.values[*]", v -> ((Integer) v).intValue() + 100);
        assertEquals("[101,102,103]", JSONArray.toJSONString(root.get("values")));
    }

    @Test
    public void test8() {
        JSONObject root = JSONObject.of("values", new int[] {1, 2, 3});
        JSONPath.set(root, "$.values[*]", 100);
        assertEquals("[100,100,100]", JSONArray.toJSONString(root.get("values")));
    }
}
