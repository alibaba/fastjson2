package com.alibaba.fastjson2.adapter.jackson.databind.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JacksonIgnoreTest {
    @Test
    public void test_0() throws Exception {
        assertEquals("{}", JSON.toJSONString(new A("101")));
        assertEquals("{}", JSON.toJSONString(new A1("101")));
        assertEquals("{}", JSON.toJSONString(new A2("101")));
        assertEquals("{}", JSON.toJSONString(new A3("101")));
    }

    @Test
    public void test_parse() throws Exception {
        String str = "{\"id\":\"101\"}";
        assertNull(
                JSON.parseObject(str, A.class)
                        .id
        );
        assertNull(
                JSON.parseObject(str, A1.class)
                        .id
        );
        assertNull(
                JSON.parseObject(str, A2.class)
                        .id
        );
    }

    public static class A {
        private String id;

        public A() {
        }

        public A(String id) {
            this.id = id;
        }

        @JsonIgnore
        public String getId() {
            return id;
        }

        @JsonIgnore
        public void setId(String id) {
            this.id = id;
        }
    }

    public static class A1 {
        @JsonIgnore
        private String id;

        public A1() {
        }

        public A1(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class A2 {
        @JsonIgnore
        public String id;

        public A2() {
        }

        public A2(String id) {
            this.id = id;
        }
    }

    public static class A3 {
        @JsonIgnore
        public final String id;

        public A3(String id) {
            this.id = id;
        }
    }
}
