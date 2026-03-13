package com.alibaba.fastjson2.v1issues.issue_1600;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@Tag("regression")
@Tag("compat-fastjson1")
public class Issue1645 {
    @Test
    public void test_for_issue() throws Exception {
        String test = "{\"name\":\"test\",\"testDateTime\":\"2017-12-08 14:55:16\"}";
        JSON.toJSONString(JSON.parseObject(test).toJavaObject(TestDateClass.class));
    }

    public static class TestDateClass {
        public String name;
        public LocalDateTime testDateTime;
    }
}
