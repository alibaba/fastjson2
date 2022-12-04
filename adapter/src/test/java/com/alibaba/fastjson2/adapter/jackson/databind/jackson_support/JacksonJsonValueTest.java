package com.alibaba.fastjson2.adapter.jackson.databind.jackson_support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonValue;
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

    @Test
    public void whenSerializingUsingJsonValue_thenCorrect() {
        assertEquals("\"Type A\"", JSON.toJSONString(TypeEnumWithValue.TYPE1));
    }

    public enum TypeEnumWithValue {
        TYPE1(1, "Type A"), TYPE2(2, "Type 2");

        private Integer id;
        private String name;

        TypeEnumWithValue(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @JsonValue
        public String getName() {
            return name;
        }
    }
}
