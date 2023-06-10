package com.alibaba.fastjson2.v1issues.issue_2200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2260 {
    @Test
    public void test_for_issue() {
        String json = "{\"date\":\"1950-07-14\"}";
        M1 m = JSON.parseObject(json, M1.class);
        assertEquals(1950, m.date.get(Calendar.YEAR));
    }

    public static class M1 {
        public Calendar date;
    }
}
