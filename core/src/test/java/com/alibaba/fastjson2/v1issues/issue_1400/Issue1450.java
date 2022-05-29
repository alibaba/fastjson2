package com.alibaba.fastjson2.v1issues.issue_1400;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1450 {
    @Test
    public void test_for_issue() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.of(2018, 8, 31, 15, 26, 37, 1);
        String json = JSON.toJSONString(localDateTime, "yyyy-MM-dd HH:mm:ss");
        assertEquals("\"2018-08-31 15:26:37\"", json);
    }
}
