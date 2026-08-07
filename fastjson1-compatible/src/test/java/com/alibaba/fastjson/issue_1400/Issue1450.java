package com.alibaba.fastjson.issue_1400;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1450 {
    @Test
    public void test_for_issue() throws Exception {
        LocalDateTime localDateTime = LocalDateTime.of(2018, 8, 31, 15, 26, 37, 1);
        String json = JSON.toJSONStringWithDateFormat(localDateTime, "yyyy-MM-dd HH:mm:ss"); //2018-08-31T15:26:37.000000001
        assertEquals("\"2018-08-31 15:26:37\"", json);
    }
}
