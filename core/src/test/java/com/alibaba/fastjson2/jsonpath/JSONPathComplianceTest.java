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
        Stat stat = new Stat();
        String resource = "jsonpath/filter.json";
        URL filterResource = JSONPathComplianceTest.class.getClassLoader().getResource(resource);
        System.out.println();
        System.out.println("--------------" + resource + "--------------");
        JSONArray tests = JSON.parseObject(filterResource)
                .getJSONArray("tests");
        for (int i = 0; i < tests.size(); i++) {
            validate(i, stat, tests.getJSONObject(i));
        }
    }

    @Test
    public void basic() {
        Stat stat = new Stat();
        String resource = "jsonpath/basic.json";
        URL filterResource = JSONPathComplianceTest.class.getClassLoader().getResource(resource);
        System.out.println();
        System.out.println("--------------" + resource + "--------------");
        JSONArray tests = JSON.parseObject(filterResource)
                .getJSONArray("tests");
        for (int i = 0; i < tests.size(); i++) {
            validate(i, stat, tests.getJSONObject(i));
        }
    }

    @Test
    public void nameSelector() {
        Stat stat = new Stat();
        String resource = "jsonpath/name_selector.json";
        URL filterResource = JSONPathComplianceTest.class.getClassLoader().getResource(resource);
        System.out.println();
        System.out.println("--------------" + resource + "--------------");
        JSONArray tests = JSON.parseObject(filterResource)
                .getJSONArray("tests");
        for (int i = 0; i < tests.size(); i++) {
            validate(i, stat, tests.getJSONObject(i));
        }
    }

    @Test
    public void indexSelector() {
        Stat stat = new Stat();
        String resource = "jsonpath/index_selector.json";
        URL filterResource = JSONPathComplianceTest.class.getClassLoader().getResource(resource);
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
                stat.errorCount ++;
                firstChar = '-';
            }
        } catch (Exception e) {
            if (!invalid_selector) {
                stat.errorCount ++;
                firstChar = '*';
            }
        }

        StringBuffer buf = new StringBuffer()
                .append(firstChar)
                .append(" [")
                .append(stat.errorCount < 10 ? "0" : "")
                .append(stat.errorCount)
                .append("/")
                .append(i < 10 ? "0" : "")
                .append(i)
                .append("]\t")
                .append(JSON.toJSONString(selector))
                ;
        System.out.println(buf);
        // validate(i, name, selector, document, results);
    }

    static class Stat {
        public int errorCount;
    }
}
