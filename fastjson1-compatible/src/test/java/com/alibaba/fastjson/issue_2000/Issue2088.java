package com.alibaba.fastjson.issue_2000;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2088 {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_for_issue() throws Exception {
        String json = "{\"date\":\"20181011103607186+0800\"}";
        Model m = JSON.parseObject(json, Model.class);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
        format.setTimeZone(JSON.defaultTimeZone);
        Date date = format.parse("20181011103607186+0800");

        assertEquals(date, m.date);
    }

    @Test
    public void test_for_issue_1() throws Exception {
        String json = "{\"date\":\"20181011103607186-0800\"}";
        Model m = JSON.parseObject(json, Model.class);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSSZ", JSON.defaultLocale);
        format.setTimeZone(JSON.defaultTimeZone);
        Date date = format.parse("20181011103607186-0800");

        assertEquals(date, m.date);
    }

    public static class Model {
        @JSONField(format = "yyyyMMddHHmmssSSSZ")
        public Date date;
    }
}
