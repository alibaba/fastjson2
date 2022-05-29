package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONPathSetTest {
    @Test
    public void test_for_issue() throws Exception {
        String text = "{\"models\":[{\"x\":\"y\"},{\"x\":\"y\"}]}";
        Root root = JSONObject.parseObject(text, Root.class);
        //System.out.println(JSON.toJSONString(root));
        String jsonpath = "$..x";
        String value = "y2";
        JSONPath.set(root, jsonpath, value);
        assertEquals("{\"models\":[{\"x\":\"y2\"},{\"x\":\"y2\"}]}", JSON.toJSONString(root));
    }

    public static class Root {
        public List<Model> models;
    }

    public static class Model {
        public String x;
    }
}
