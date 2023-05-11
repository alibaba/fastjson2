package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1460 {
    @Test
    public void test() {
        String value = "{\"username\":\"zhangsan\"}";
        User user = JSON.parseObject(value, User.class, JSONReader.Feature.SupportSmartMatch);
        assertEquals("zhangsan", user.userName);
    }

    class User {
        private String userName;

        public User(String userName) {
            this.userName = userName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}
