package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1131 {
    @Test
    public void test() {
        B b = new B();
        b.setCity("c1");
        String str = JSON.toJSONString(b);
        assertEquals("{\"city_c\":\"c1\"}", str);
    }

    public static class B
            extends C{
    }

    public static class C {
        @JSONField(name = "no-c")
        private String no;

        @JSONField(name = "city_c")
        private String city;

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getNo() {
            return no;
        }
        public void setNo(String no) {
            this.no = no;
        }
    }
}
