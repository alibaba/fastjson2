package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author kraity
 */
public class TypeReferenceTest {

    @Test
    public void test_parseObject() {
        String text = "{\"id\":1,\"name\":\"kraity\"}";

        User user = new TypeReference<User>() {
        }.parseObject(text);

        assertEquals(text, JSON.toJSONString(user));
    }

    @Test
    public void test_parseArray() {
        String text = "[{\"id\":1,\"name\":\"kraity\"}]";

        List<User> users = new TypeReference<User>() {
        }.parseArray(text);

        assertEquals(text, JSON.toJSONString(users));
    }

    static class User {
        int id;
        String name;

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
