package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class JSONPathComplianceTest {
    @Test
    public void filter() {
        URL filterResource = JSONPathComplianceTest.class.getClassLoader().getResource("jsonpath_filter.json");
        JSONArray tests = JSON.parseObject(filterResource)
                .getJSONArray("tests");
        for (int i = 0; i < tests.size(); i++) {
            JSONObject test = tests.getJSONObject(i);
            String name = test.getString("name");
            String selector = test.getString("selector");
            Object document = test.get("document");
            Object results = test.get("results");

            validate(i, name, selector, document, results);
        }
    }

    static void validate(int i, String name, String selector, Object document, Object results) {
        Exception error = null;
        try {
            JSONPath path = JSONPath.of(selector);
        } catch (Exception e) {
            error = e;
        }
        if (error != null) {
            System.err.println("jsonpath syntax [" + i + "] error" + selector + ", " + error);
        }
    }
}
