package com.alibaba.fastjson.issue_1100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 05/05/2017.
 */
public class Issue1177_2 {
    @Test
    public void test_for_issue() throws Exception {
        String text = "{\"a\":{\"x\":\"y\"},\"b\":{\"x\":\"y\"}}";
        Map<String, Model> jsonObject = JSONObject.parseObject(text, new TypeReference<Map<String, Model>>() {
        }.getType());
        System.out.println(JSON.toJSONString(jsonObject));
        String jsonpath = "$..x";
        String value = "y2";
        JSONPath.set(jsonObject, jsonpath, value);
        assertEquals("{\"a\":{\"x\":\"y2\"},\"b\":{\"x\":\"y2\"}}", JSON.toJSONString(jsonObject));
    }

    public static class Model {
        public String x;
    }
}
