package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FluentSetterTest {
    @Test
    public void test_fluent() throws Exception {
        B b = new B();
        b.setId(1001);
        b.setValue(1002);

        String text = JSON.toJSONString(b);
        assertEquals("{\"id\":1001,\"value\":1002}", text);

        B b1 = JSON.parseObject(text, B.class);
        assertEquals(b.getId(), b1.getId());
        assertEquals(b.getValue(), b1.getValue());
    }

    public static class A {
        private int id;

        public int getId() {
            return id;
        }

        public A setId(int id) {
            this.id = id;
            return this;
        }
    }

    public static class B
            extends A {
        private int value;

        public int getValue() {
            return value;
        }

        public B setValue(int value) {
            this.value = value;
            return this;
        }
    }
}
