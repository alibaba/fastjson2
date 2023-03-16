package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1246 {
    @Test
    public void test() {
        User user = new User();
        user.setP2("asda");
        assertEquals("{\"aaaa\":\"asda\"}", JSON.toJSONString(user));
    }

    static class User {
        @JSONField(name = "aaaa")
        private String P2;

        public String getP2() {
            return P2;
        }

        public void setP2(String p2) {
            P2 = p2;
        }
    }
}
