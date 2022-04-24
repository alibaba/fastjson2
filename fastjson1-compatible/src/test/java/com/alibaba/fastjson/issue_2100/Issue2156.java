package com.alibaba.fastjson.issue_2100;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2156 {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

//    public void test_for_issue() throws Exception {
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
//        sf.setTimeZone(JSON.defaultTimeZone);
//        java.sql.Date date = new java.sql.Date(sf.parse("2018-07-15").getTime());
//        String str = JSON.toJSONStringWithDateFormat(date, JSON.DEFFAULT_DATE_FORMAT);
//        assertEquals("\"2018-07-15\"", str);
//    }
//
//    public void test_for_issue1() throws Exception {
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
//        sf.setTimeZone(JSON.defaultTimeZone);
//        java.sql.Date date = new java.sql.Date(sf.parse("2018-07-15").getTime());
//        String str = JSON.toJSONStringWithDateFormat(date, JSON.DEFFAULT_DATE_FORMAT);
//        assertEquals("\"2018-07-15\"", str);
//    }
//
//    public void test_for_issue2() throws Exception {
//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
//        sf.setTimeZone(JSON.defaultTimeZone);
//        java.sql.Date date = java.sql.Date.valueOf("2018-07-15");
//        String str = JSON.toJSONStringWithDateFormat(date, JSON.DEFFAULT_DATE_FORMAT);
//        assertEquals("\"2018-07-15\"", str);
//    }

    @Test
    public void test_for_issue_time() throws Exception {
        java.sql.Time date = java.sql.Time.valueOf("12:13:14");
        String str = JSON.toJSONStringWithDateFormat(date, JSON.DEFFAULT_DATE_FORMAT);
        assertEquals("\"12:13:14\"", str);
    }
}
