package com.alibaba.fastjson2.v1issues.issue_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3969 {
    @Test
    public void test_for_issue3969() {
        A a = new A();
        a.s = "str";
        a.c = '0';
        String testJson = JSON.toJSONString(a);
        assertEquals("{\"c\":\"0\",\"s\":\"str\"}", testJson);
        JSONArray jsa = JSON.parseArray("[" + testJson + "]");
        List<A> list = jsa.toJavaList(A.class);
        assertEquals("[A{s='str', c=0}]", list.toString());
    }

    @Data
    public static class A {
        private String s;
        private Character c;

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public Character getC() {
            return c;
        }

        public void setC(Character c) {
            this.c = c;
        }

        @Override
        public String toString() {
            return "A{" + "s='" + s + '\'' + ", c=" + c + '}';
        }
    }
}
