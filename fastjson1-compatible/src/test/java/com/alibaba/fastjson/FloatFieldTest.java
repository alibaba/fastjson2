package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatFieldTest {
    @Test
    public void test_codec() throws Exception {
        User user = new User();
        user.setValue(1001F);

        String text = JSON.toJSONString(user);
        assertEquals("{\"value\":1001.0}", text);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(user1.getValue(), user.getValue());
    }

    public static class User {
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }
}
