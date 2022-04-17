package com.alibaba.fastjson2.jsonpath;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import junit.framework.TestCase;

public class JSONPath_13
        extends TestCase {

    public void test_0() {
        JSONObject root = new JSONObject();
        root.put("company", new JSONObject());
        root.getJSONObject("company").put("id", 123);
        root.getJSONObject("company").put("name", "jobs");

        JSONPath.of("$..id")
                .remove(root);

        assertEquals("{\"company\":{\"name\":\"jobs\"}}", JSON.toJSONString(root));
    }

    public void test_1() {
        Root root = new Root();
        root.company = new Company();
        root.company.id = 123;
        root.company.name = "jobs";

        JSONPath.of("$..id")
                .remove(root);

        assertEquals("{\"company\":{\"name\":\"jobs\"}}", JSON.toJSONString(root));
    }

    public static class Root {
        public Company company;
    }

    public static class Company {
        public Integer id;
        public String name;
    }
}
