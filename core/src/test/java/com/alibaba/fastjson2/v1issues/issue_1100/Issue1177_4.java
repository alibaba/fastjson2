package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 05/05/2017.
 */
public class Issue1177_4 {
    @Test
    public void test_for_issue() throws Exception {
        String text = "{\"models\":[{\"x\":\"y\"},{\"x\":\"y\"}]}";
        Root root = JSON.parseObject(text, Root.class);
        System.out.println(JSON.toJSONString(root));
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
