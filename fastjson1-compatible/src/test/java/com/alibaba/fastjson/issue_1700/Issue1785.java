package com.alibaba.fastjson.issue_1700;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue1785 {
    @Test
    public void test_for_issue() throws Exception {
        Timestamp timestamp = JSON.parseObject("\"2006-8-9\"", Timestamp.class);
        assertNotNull(timestamp);
    }
}
