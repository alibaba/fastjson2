package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.util.TypeUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class Issue3901 {
    @BeforeEach
    public void setup() {
        JSON.register(User.class, new UserReader());
    }

    @AfterEach
    public void tearDown() {
        JSON.register(User.class, (ObjectReader) null);
    }

    @Test
    public void testJSONObjectTo() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", 1001);
        jsonObject.put("name", "fastjson");

        User user = jsonObject.to(User.class); // 底层调用 createInstance(Map)

        Assertions.assertEquals(1001, user.id);
        Assertions.assertEquals("FASTJSON", user.name);
    }

    @Test
    public void testJSONObjectGetObject() {
        JSONObject root = new JSONObject();
        JSONObject userJson = new JSONObject();
        userJson.put("id", 1002);
        userJson.put("name", "fastjson");
        root.put("userInfo", userJson);

        User user = root.getObject("userInfo", User.class); // 底层调用 createInstance(Map)

        Assertions.assertEquals(1002, user.id);
        Assertions.assertEquals("FASTJSON", user.name);
    }

    @Test
    public void testJSONArrayTo() {
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(1003);
        jsonArray.add("array_test");

        User user = jsonArray.to(User.class); // 底层调用 createInstance(Collection)

        Assertions.assertEquals(1003, user.id);
        Assertions.assertEquals("ARRAY_TEST", user.name);
    }

    public static class UserReader implements ObjectReader<User> {
        /**
         * 用户如果注册了自定义反序列化器，调用以下方法前，需要重写 createInstance(Map map, long features)：
         *
         * JSONObject：
         * getObject(String key, Class<T> type, ...)
         * getObject(String key, Type type, ...)
         * to(Class<T> clazz, ...)
         * to(Type type, ...)
         * to(TypeReference<T> ...)
         * toJavaObject(...)
         *
         * JSONArray：
         * getObject(int index, Class<T> type, ...)
         * getObject(int index, Type type, ...)
         * toArray(Class<T> itemClass, ...)
         * toJavaList(...)
         * toList(Class<T> itemClass, ...)
         */
        @Override
        public User createInstance(Map map, long features) {
            if (map == null) {
                return null;
            }
            User user = new User();

            Object id = map.get("id");
            Object name = map.get("name");

            user.id = TypeUtils.toIntValue(id);
            String nameStr = TypeUtils.cast(name, String.class);
            user.name = nameStr != null ? nameStr.toUpperCase() : null;
            return user;
        }

        /**
         * 用户如果注册了自定义反序列化器，调用以下方法前，需要重写 createInstance(Collection collection, long features)：
         *
         * JSONObject：
         * getObject(String key, Class<T> type, ...)
         * getObject(String key, Type type, ...)
         *
         * JSONArray：
         * getObject(int index, Class<T> type, ...)
         * getObject(int index, Type type, ...)
         * to(Class<T> type)
         * to(Type type)
         * toJavaObject(...) (Deprecated)
         */
        @Override
        public User createInstance(Collection collection, long features) {
            if (collection == null || collection.isEmpty()) {
                return null;
            }
            User user = new User();
            Iterator<?> iterator = collection.iterator();

            if (iterator.hasNext()) {
                user.id = TypeUtils.toIntValue(iterator.next());
            }
            if (iterator.hasNext()) {
                String nameStr = TypeUtils.cast(iterator.next(), String.class);
                user.name = nameStr != null ? nameStr.toUpperCase() : null;
            }
            return user;
        }

        @Override
        public User readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            return new User();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        public int id;
        public String name;
    }
}
