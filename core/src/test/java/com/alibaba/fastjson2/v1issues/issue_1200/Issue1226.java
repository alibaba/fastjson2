package com.alibaba.fastjson2.v1issues.issue_1200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1226 {
    @Test
    public void test_for_issue() throws Exception {
        String json = "{\"c\":\"c\"}";
        TestBean tb1 = JSON.parseObject(json, TestBean.class);
        assertEquals('c', tb1.getC());

        TestBean2 tb2 = JSON.parseObject(json, TestBean2.class);
        assertEquals('c', tb2.getC().charValue());

        String json2 = JSON.toJSONString(tb2);
        JSONObject jo = JSON.parseObject(json2);

        TestBean tb12 = jo.toJavaObject(TestBean.class);
        assertEquals('c', tb12.getC());

        TestBean2 tb22 = jo.toJavaObject(TestBean2.class);
        assertEquals('c', tb22.getC().charValue());
    }

    static class TestBean {
        char c;

        public char getC() {
            return c;
        }

        public void setC(char c) {
            this.c = c;
        }
    }

    static class TestBean2 {
        Character c;

        public Character getC() {
            return c;
        }

        public void setC(Character c) {
            this.c = c;
        }
    }
}
