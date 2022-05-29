package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OverrideTest {
    @Test
    public void test_nullAsEmptyStr() {
        B b = new B();

        String json = JSON.toJSONString(b, JSONWriter.Feature.WriteNulls, JSONWriter.Feature.NullAsDefaultValue);
        assertEquals("{\"value\":\"\"}", json);
    }

    public abstract static class A {
        public abstract Object getValue();
    }

    public static class B
            extends A {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
