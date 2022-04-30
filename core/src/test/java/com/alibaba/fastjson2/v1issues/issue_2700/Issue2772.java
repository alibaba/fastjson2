package com.alibaba.fastjson2.v1issues.issue_2700;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2772 {
    @Test
    public void test_for_issue() throws Exception {
        {
            java.sql.Time time = java.sql.Time.valueOf("12:13:14");
            long millis = time.getTime();
            assertEquals(Long.toString(millis / 1000), JSON.toJSONString(time, "unixtime"));
            assertEquals(Long.toString(millis), JSON.toJSONString(time, "millis"));
        }

        long millis = System.currentTimeMillis();
        assertEquals(Long.toString(millis), JSON.toJSONString(new Date(millis), "millis"));
        assertEquals(Long.toString(millis / 1000), JSON.toJSONString(new Date(millis), "unixtime"));
    }
}
