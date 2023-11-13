package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue2003 {
    @Test
    public void testNullWithToJson() {
        A a = new A();
        a.setA1(null);
        a.setA2("a2");
        JSONObject jo = (JSONObject) JSON.toJSON(a);
        assertFalse(jo.containsKey("a1")); // 这里不能通过！
    }

    @Test
    public void testNullWithToJsonString() {
        A a = new A();
        a.setA1(null);
        a.setA2("a2");
        String str = JSON.toJSONString(a);
        assertFalse(str.contains("a1"));  // 这里通过了
    }

    public static class A {
        private String a1;
        private String a2;

        public String getA1() {
            return a1;
        }

        public void setA1(String a1) {
            this.a1 = a1;
        }

        public String getA2() {
            return a2;
        }

        public void setA2(String a2) {
            this.a2 = a2;
        }
    }
}
