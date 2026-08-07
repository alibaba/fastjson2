package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_date() throws Exception {
        long millis = 1324138987429L;
        Date date = new Date(millis);

        assertEquals("1324138987429", JSON.toJSONString(date));
        assertEquals("new Date(1324138987429)", JSON.toJSONString(date, SerializerFeature.WriteClassName));

        assertEquals("\"2011-12-18 00:23:07\"", JSON.toJSONString(date, SerializerFeature.WriteDateUseDateFormat));
        assertEquals("\"2011-12-18 00:23:07.429\"", JSON.toJSONStringWithDateFormat(date, "yyyy-MM-dd HH:mm:ss.SSS"));
    }

    @Test
    public void test_parse() throws Exception {
        Date date = JSON.parseObject("\"2018-10-12 09:48:22 +0800\"", Date.class);
        assertEquals(1539308902000L, date.getTime());
    }
}
