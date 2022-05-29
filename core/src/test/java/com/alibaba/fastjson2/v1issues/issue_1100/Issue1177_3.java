package com.alibaba.fastjson2.v1issues.issue_1100;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by wenshao on 05/05/2017.
 */
public class Issue1177_3 {
    @Test
    public void test_for_issue() throws Exception {
        String text = "[{\"x\":\"y\"},{\"x\":\"y\"}]";
        List<Model> jsonObject = JSON.parseObject(text, new TypeReference<List<Model>>() {
        }.getType());
        System.out.println(JSON.toJSONString(jsonObject));
        String jsonpath = "$..x";
        String value = "y2";
        JSONPath.set(jsonObject, jsonpath, value);
        assertEquals("[{\"x\":\"y2\"},{\"x\":\"y2\"}]", JSON.toJSONString(jsonObject));
    }

    public static class Model {
        public String x;
    }
}
