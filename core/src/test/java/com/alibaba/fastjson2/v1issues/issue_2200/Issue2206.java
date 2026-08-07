package com.alibaba.fastjson2.v1issues.issue_2200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

@Tag("regression")
@Tag("compat-fastjson1")
public class Issue2206 {
    @Test
    public void test_for_issue() throws Exception {
        JSON.parseObject("{\"date\":\"20181229162849\"}", Model.class);
    }

    public static class Model {
        public LocalDateTime date;
    }
}
