package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class JSONPathComplianceSupportTest {
    @Test
    public void filter() {
        Stat stat = new Stat();
        String resource = "jsonpath_compliance.json";
        URL filterResource = JSONPathComplianceSupportTest.class.getClassLoader().getResource(resource);
        System.out.println();
        System.out.println("--------------" + resource + "--------------");
        JSONArray tests = JSON.parseObject(filterResource)
                .getJSONArray("tests");
        for (int i = 0; i < tests.size(); i++) {
            validate(i, stat, tests.getJSONObject(i));
        }
    }

    static void validate(int i, Stat stat, JSONObject test) {
        String name = test.getString("name");
        String selector = test.getString("selector");
        Object document = test.get("document");
        Object results = test.get("results");
        boolean invalid_selector = test.getBooleanValue("invalid_selector");

        char firstChar = ' ';
        Exception error = null;
        try {
            JSONPath path = JSONPath.of(selector);
            if (invalid_selector) {
                stat.errorCount++;
                firstChar = '-';
            }
        } catch (Exception e) {
            if (!invalid_selector) {
                stat.errorCount++;
                firstChar = '*';
            }
        }

        String buf = firstChar
                + " ["
                + (stat.errorCount < 10 ? "0" : "")
                + stat.errorCount
                + "/"
                + (i < 10 ? "0" : "")
                + i
                + "]\t"
                + JSON.toJSONString(selector);
        System.out.println(buf);
        // validate(i, name, selector, document, results);
    }

    static class Stat {
        public int errorCount;
    }
}
