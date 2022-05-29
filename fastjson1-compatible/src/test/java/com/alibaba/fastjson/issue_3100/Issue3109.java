package com.alibaba.fastjson.issue_3100;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

public class Issue3109 {
    @Test
    public void test_for_issue() throws Exception {
        JSON.parseObject("{\"@type\":\"testxx\",\"dogName\":\"dog1001\"}", Dog.class);
    }

    public static class Dog {
        public String dogName;
    }
}
