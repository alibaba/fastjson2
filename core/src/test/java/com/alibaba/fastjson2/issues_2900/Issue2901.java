package com.alibaba.fastjson2.issues_2900;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue2901 {
    @Setter
    @Getter
    @AllArgsConstructor
    public class User {
        @JSONField(name = "user_name")
        private String userName;

        @Override
        public String toString() {
            return "User{" +
                    "userName='" + userName + '\'' +
                    '}';
        }
    }

    @Test
    void test() {
        String str1 = "{\n" +
                "\"user_name\":\"zs\"\n" +
                "}";
        User user = JSONObject.parseObject(str1, User.class);
        assertNotNull(user);
        assertEquals(user.getUserName(), "zs");
    }
}
