package com.alibaba.fastjson.issue_2700;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2754 {
    @Test
    public void test_for_issue0() throws Exception {
        String s = "{\"p1\":\"2019-09-18T20:35:00+12:45\"}";
        C c = JSON.parseObject(s, C.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Pacific/Chatham"));
        assertEquals("2019-09-18T20:35:00+12:45", sdf.format(c.p1.getTime()));
    }

    @Test
    public void test_for_issue1() throws Exception {
        String s = "{\"p1\":\"2019-09-18T20:35:00+12:45\"}";
        C c = JSON.parseObject(s, C.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("NZ-CHAT"));
        assertEquals("2019-09-18T20:35:00+12:45", sdf.format(c.p1.getTime()));
    }

    @Test
    public void test_for_issue2() throws Exception {
        String s = "{\"p1\":\"2019-09-18T20:35:00+05:45\"}";
        C c = JSON.parseObject(s, C.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kathmandu"));
        assertEquals("2019-09-18T20:35:00+05:45", sdf.format(c.p1.getTime()));
    }

    @Test
    public void test_for_issue3() throws Exception {
        String s = "{\"p1\":\"2019-09-18T20:35:00+05:45\"}";
        C c = JSON.parseObject(s, C.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Katmandu"));
        assertEquals("2019-09-18T20:35:00+05:45", sdf.format(c.p1.getTime()));
    }

    @Test
    public void test_for_issue4() throws Exception {
        String s = "{\"p1\":\"2019-09-18T20:35:00+08:45\"}";
        C c = JSON.parseObject(s, C.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("Australia/Eucla"));
        assertEquals("2019-09-18T20:35:00+08:45", sdf.format(c.p1.getTime()));
    }

    public static class C {
        public Calendar p1;
    }
}
