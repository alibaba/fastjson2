package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2503 {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject("{\"sinkAAAAAAAA\":\"A\"}");
        assertEquals("A", object.get("sinkAAAAAAAA"));

        JSONObject object2 = JSON.parseObject("{\"skAAAAAAAA\":\"A\"}");
        assertEquals("A", object2.get("skAAAAAAAA"));

        JSONObject object3 = JSON.parseObject("{\"saakAAAAAAAA\":\"A\"}");
        assertEquals("A", object3.get("saakAAAAAAAA"));

        JSONObject object4 = JSON.parseObject("{\"SkAAAAAAAA\":\"A\"}");
        assertEquals("A", object4.get("SkAAAAAAAA"));
    }
}
