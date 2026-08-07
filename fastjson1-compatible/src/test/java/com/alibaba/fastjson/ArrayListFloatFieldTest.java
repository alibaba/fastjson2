package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayListFloatFieldTest {
    @Test
    public void test_codec() throws Exception {
        User user = new User();
        user.setValue(new ArrayList<Float>());
        user.getValue().add(1F);

        String text = JSON.toJSONString(user);
        System.out.println(text);

        User user1 = JSON.parseObject(text, User.class);

        Float actual = user1.getValue().get(0);
        assertEquals(user.getValue().get(0), actual);
    }

    public static class User {
        private ArrayList<Float> value;

        public User() {
        }

        public List<Float> getValue() {
            return value;
        }

        public void setValue(ArrayList<Float> value) {
            this.value = value;
        }
    }
}
