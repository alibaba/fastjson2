package com.alibaba.fastjson2.v1issues.issue_1700;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1766 {
    @Test
    public void test_for_issue() throws Exception {
// succ
        String json = "{\"name\":\"张三\"\n, \"birthday\":\"2017-01-01 01:01:01\"}";
        User user = JSON.parseObject(json, User.class);
        assertEquals("张三", user.getName());
        assertNotNull(user.getBirthday());

        // failed
        json = "{\"name\":\"张三\", \"birthday\":\"2017-01-01 01:01:02\"\n}";
        user = JSON.parseObject(json, User.class); // will exception
        assertEquals("张三", user.getName());
        assertNotNull(user.getBirthday());
    }

    public static class User {
        private String name;

        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date birthday;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }
    }
}
