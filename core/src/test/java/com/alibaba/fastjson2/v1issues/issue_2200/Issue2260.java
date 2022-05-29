package com.alibaba.fastjson2.v1issues.issue_2200;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2260 {
    @Test
    public void test_for_issue() {
        String json = "{\"date\":\"1950-07-14\"}";
        M1 m = JSON.parseObject(json, M1.class);
        assertEquals(1950, m.date.get(Calendar.YEAR));
    }

    @Test
    public void test_for_jdk8_zdt_1() {
        String json = "{\"date\":\"1950-07-14\"}";
        M2 m = JSON.parseObject(json, M2.class);
        assertEquals(1950, m.date.getYear());
    }

    @Test
    public void test_for_jdk8_zdt_2() {
        String json = "{\"date\":\"1950-07-14 12:23:34\"}";
        M2 m = JSON.parseObject(json, M2.class);
        assertEquals(1950, m.date.getYear());
    }

    @Test
    public void test_for_jdk8_zdt_3() {
        String json = "{\"date\":\"1950-07-14T12:23:34\"}";
        M2 m = JSON.parseObject(json, M2.class);
        assertEquals(1950, m.date.getYear());
    }

    @Test
    public void test_for_jdk8_ldt_1() {
        String json = "{\"date\":\"1950-07-14\"}";
        M3 m = JSON.parseObject(json, M3.class);
        assertEquals(1950, m.date.getYear());
    }

    @Test
    public void test_for_jdk8_ldt_2() {
        String json = "{\"date\":\"1950-07-14 12:23:34\"}";
        M3 m = JSON.parseObject(json, M3.class);
        assertEquals(1950, m.date.getYear());
    }

    @Test
    public void test_for_jdk8_ldt_3() {
        String json = "{\"date\":\"1950-07-14T12:23:34\"}";
        M3 m = JSON.parseObject(json, M3.class);
        assertEquals(1950, m.date.getYear());
    }

    public static class M1 {
        public Calendar date;
    }

    public static class M2 {
        public ZonedDateTime date;
    }

    public static class M3 {
        public LocalDateTime date;
    }
}
