package com.alibaba.fastjson2.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.annotation.JsonValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonJsonValueTest {
    @Test
    public void test() {
        Address address = new Address("Hangzhou");
        String str = JSON.toJSONString(address);
        assertEquals("\"Hangzhou\"", str);
    }

    public static class Address {
        @JsonValue
        public final String name;

        public Address(String name) {
            this.name = name;
        }
    }
}
