package com.alibaba.fastjson.issue_2200;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.List;

public class Issue2234 {
    @Test
    public void test_for_issue() throws Exception {
        String userStr = "{\"name\":\"asdfad\",\"ss\":\"\"}";
        User user = JSON.parseObject(userStr, User.class);
    }

    public static class User {
        public String name;
        public List ss;
    }
}
