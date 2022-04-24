package com.alibaba.json.bvt.issue_1400;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.parser.Feature;
import junit.framework.TestCase;

import java.util.List;

public class Issue1429 extends TestCase {
    public void test_for_issue() throws Exception {
        String json = "[{\n" +
                "            \"@type\": \"com.alibaba.json.bvt.issue_1400.Issue1429$Student\",\n" +
                "            \"age\": 22,\n" +
                "            \"id\": 1,\n" +
                "            \"name\": \"hello\"\n" +
                "        }, {\n" +
                "            \"age\": 22,\n" +
                "            \"id\": 1,\n" +
                "            \"name\": \"hhh\",\n" +
                "            \"@type\": \"com.alibaba.json.bvt.issue_1400.Issue1429$Student\"\n" +
                "        }]";

        JSONArray list = JSON.parseArray(json, Feature.SupportAutoType);
        Student s0 = (Student) list.get(0);
        assertEquals(1, s0.id);
        assertEquals(22, s0.age);
        assertEquals("hello", s0.name);

        Student s1 = list.getObject(1, Student.class);
        assertEquals(1, s1.id);
        assertEquals(22, s1.age);
        assertEquals("hhh", s1.name);
    }

    public static class Student {
        public int id;
        public int age;
        public String name;
    }
}
