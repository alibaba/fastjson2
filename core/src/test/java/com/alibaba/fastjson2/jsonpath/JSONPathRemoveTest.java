package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathRemoveTest {
    @Test
    public void test() {
        JSONArray array = JSON.parseArray("[0,1,2,3]");
        JSONPath.remove(array, "$[?(@ == 1)]");
        assertEquals("[0,2,3]", array.toJSONString());
    }

    @Test
    public void test_str() {
        assertEquals(
                "[0,2,3]",
                JSONPath.remove("[0,1,2,3]", "$[?(@ == 1)]")
        );
    }

    @Test
    public void test1() {
        JSONObject object = JSON.parseObject("{'store':{'book':['x0','x1']}}");
        JSONPath.remove(object, "$.store.book[0]");
        assertEquals("{\"store\":{\"book\":[\"x1\"]}}", object.toJSONString());
    }

    @Test
    public void test1_str() {
        assertEquals(
                "{\"store\":{\"book\":[\"x1\"]}}",
                JSONPath.remove("{'store':{'book':['x0','x1']}}", "$.store.book[0]")
        );
    }

    @Test
    public void test2() {
        JSONObject object = JSON.parseObject("{'store':{'book':['x0','x1','x2','x3']}}");
        JSONPath.remove(object, "$.store.book[0:2]");
        assertEquals("{\"store\":{\"book\":[\"x2\",\"x3\"]}}", object.toJSONString());
    }

    @Test
    public void test2_str() {
        assertEquals("{\"store\":{\"book\":[\"x2\",\"x3\"]}}",
                JSONPath.remove("{'store':{'book':['x0','x1','x2','x3']}}", "$.store.book[0:2]"));
    }

    @Test
    public void test3() {
        assertEquals("{\"store\":{}}",
                JSONPath.remove("{'store':{'book':['x0','x1','x2','x3']}}", "$.store.*"));
    }

    @Test
    public void test4() {
        assertEquals("{\"store\":{\"book\":[]}}",
                JSONPath.remove("{'store':{'book':['x0','x1','x2','x3']}}", "$.store.book[*]"));
    }

    @Test
    public void test5() {
        assertEquals("{\"store\":{\"book\":[]}}",
                JSONPath.remove("{'store':{'book':['x0','x1','x2','x3']}}", "$.store.book[*]"));
    }
}
