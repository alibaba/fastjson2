package com.alibaba.fastjson2.annotation;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONField_value {
    @Test
    public void test() {
        Address address = new Address("Hangzhou");
        String str = JSON.toJSONString(address);
        assertEquals("\"Hangzhou\"", str);
    }

    public static class Address {
        @JSONField(value = true)
        public final String name;

        public Address(String name) {
            this.name = name;
        }
    }
}
