package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        User user1 = new TypeReference<User>() {
        }
                .toJavaObject(JSON.parseObject(text));

        assertEquals(user.id, user1.id);
        assertEquals(user.name, user1.name);
    }

    @Test
    public void test_parseArray() {
        String text = "[{\"id\":1,\"name\":\"kraity\"}]";

        List<User> users = new TypeReference<User>() {
        }.parseArray(text);

        assertEquals(text, JSON.toJSONString(users));
    }

    @Test
    public void test_toJavaObject() {
        String text = "[{\"id\":1,\"name\":\"kraity\"}]";
        JSONArray array = JSON.parseArray(text);
        JSONObject object = array.getJSONObject(0);

        User user = new TypeReference<User>() {
        }.toJavaObject(object);
        assertEquals(1, user.id);
        assertEquals("kraity", user.name);

        List<User> users = new TypeReference<List<User>>() {
        }.toJavaObject(array);
        assertEquals(1, users.size());
        assertEquals(1, users.get(0).id);
        assertEquals("kraity", users.get(0).name);
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

    @Test
    public void testError() {
        assertThrows(NullPointerException.class,
                () -> TypeReference.get(null)
        );
    }

    @Test
    public void testError1() {
        assertThrows(NullPointerException.class,
                () -> error1(User.class)
        );
    }

    @Test
    public void testError2() {
        assertThrows(NullPointerException.class,
                () -> error1(User.class)
        );
    }

    public static <T> void error1(Class<T> clazz) {
        new TypeReference<List<T>>((Type[]) null) {
        };
    }

    public static <T> void error2(Class<T> clazz) {
        new TypeReference<List<T>>(new Type[0]) {
        };
    }
}
