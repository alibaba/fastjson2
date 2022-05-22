package com.alibaba.fastjson.date;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFieldTest9 {
    @BeforeEach
    protected void setUp() throws Exception {
        JSON.defaultTimeZone = TimeZone.getTimeZone("Asia/Shanghai");
        JSON.defaultLocale = Locale.CHINA;
    }

    @Test
    public void test_tw() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016/05/06\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_cn() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016-05-06\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_cn_1() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016年5月6日\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_cn_2() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016年5月06日\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_cn_3() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016年05月6日\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_cn_4() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016年05月06日\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_kr_1() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016년5월6일\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_kr_2() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016년5월06일\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_kr_3() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016년05월6일\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_kr_4() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"2016년05월06일\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_de() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"06.05.2016\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    @Test
    public void test_in() throws Exception {
        Entity vo = JSON.parseObject("{\"date\":\"06-05-2016\"}", Entity.class);

        Calendar calendar = Calendar.getInstance(JSON.defaultTimeZone, JSON.defaultLocale);
        calendar.setTime(vo.date);
        Assertions.assertEquals(2016, calendar.get(Calendar.YEAR));
        Assertions.assertEquals(4, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        Assertions.assertEquals(0, calendar.get(Calendar.MINUTE));
        Assertions.assertEquals(0, calendar.get(Calendar.SECOND));
        Assertions.assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    public static class Entity {
        public Date date;
    }
}
