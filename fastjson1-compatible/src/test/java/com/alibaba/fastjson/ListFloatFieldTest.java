package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ListFloatFieldTest {
    @Test
    public void test_codec() throws Exception {
        User user = new User();
        user.setValue(new ArrayList<Float>());
        user.getValue().add(1F);

        String text = JSON.toJSONString(user);

        User user1 = JSON.parseObject(text, User.class);

        assertEquals(user1.getValue(), user.getValue());
    }

    public static class User {
        private List<Float> value;

        public List<Float> getValue() {
            return value;
        }

        public void setValue(List<Float> value) {
            this.value = value;
        }
    }
}
