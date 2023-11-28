package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Map;

/**
 * Created by wenshao on 05/05/2017.
 */
public class Issue1177_2 {
    @Test
    public void test_for_issue() throws Exception {
        String text = "{\"a\":{\"x\":\"y\"},\"b\":{\"x\":\"y\"}}";
        Map<String, Model> jsonObject = JSON.parseObject(text, new TypeReference<Map<String, Model>>() {
        }.getType());
        System.out.println(JSON.toJSONString(jsonObject));
        String jsonpath = "$..x";
        String value = "y2";
        JSONPath.set(jsonObject, jsonpath, value);
        JSONAssert.assertEquals("{\"a\":{\"x\":\"y2\"},\"b\":{\"x\":\"y2\"}}", JSON.toJSONString(jsonObject), true);
    }

    public static class Model {
        public String x;
    }
}
