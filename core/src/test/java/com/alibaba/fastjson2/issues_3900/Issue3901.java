package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3901 {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "James");

        JSON.register(User.class, new UserReader());
        User user = jsonObject.to(User.class);
        assertEquals("ZhangSan", user.getName());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private String name;
    }

    public static class UserReader implements ObjectReader {
        @Override
        public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
            jsonReader.readObject();
            return new User("ZhangSan");
        }
    }
}
