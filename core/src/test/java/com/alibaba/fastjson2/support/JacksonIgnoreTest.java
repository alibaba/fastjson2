package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonIgnoreTest {
    @Test
    public void test_0() throws Exception {
        assertEquals("{}", JSON.toJSONString(new A("101")));
        assertEquals("{}", JSON.toJSONString(new A1("101")));
        assertEquals("{}", JSON.toJSONString(new A2("101")));
    }

    public static class A {
        private String id;

        public A(String id) {
            this.id = id;
        }

        @JsonIgnore
        public String getId() {
            return id;
        }
    }


    public static class A1 {
        @JsonIgnore
        private String id;

        public A1(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public static class A2 {
        @JsonIgnore
        public final String id;

        public A2(String id) {
            this.id = id;
        }
    }
}
