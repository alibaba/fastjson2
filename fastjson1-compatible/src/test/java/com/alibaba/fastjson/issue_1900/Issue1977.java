package com.alibaba.fastjson.issue_1900;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1977 {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_for_issue() throws Exception {
        java.sql.Date date = new java.sql.Date(1533265119604L);
        String json = JSON.toJSONString(date, SerializerFeature.UseISO8601DateFormat);
        assertEquals("\"2018-08-03T10:58:39.604+08:00\"", json);
//        new java.sql.Date();
    }
}
