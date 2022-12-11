package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RFC1123Test {
    static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    public void test28() {
        String str = "\"Tue, 3 Jun 2008 11:05:30 GMT\"";
        {
            Date date = JSON.parseObject(str, Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(UTC);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(UTC);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(UTC);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
    }

    @Test
    public void test29() {
        String str = "\"Sun, 11 Dec 2022 12:21:20 GMT\"";
        {
            Date date = JSON.parseObject(str, Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(UTC);
            assertEquals(2022, zdt.getYear());
            assertEquals(12, zdt.getMonthValue());
            assertEquals(11, zdt.getDayOfMonth());
            assertEquals(12, zdt.getHour());
            assertEquals(21, zdt.getMinute());
            assertEquals(20, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(UTC);
            assertEquals(2022, zdt.getYear());
            assertEquals(12, zdt.getMonthValue());
            assertEquals(11, zdt.getDayOfMonth());
            assertEquals(12, zdt.getHour());
            assertEquals(21, zdt.getMinute());
            assertEquals(20, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(UTC);
            assertEquals(2022, zdt.getYear());
            assertEquals(12, zdt.getMonthValue());
            assertEquals(11, zdt.getDayOfMonth());
            assertEquals(12, zdt.getHour());
            assertEquals(21, zdt.getMinute());
            assertEquals(20, zdt.getSecond());
        }
    }

    @Test
    public void parseDate() {
        {
            String str = "Tue, 3 Jun 2008 11:05:30 GMT";
            Date date = DateUtils.parseDate(str);
            ZonedDateTime zdt = date.toInstant().atZone(UTC);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
        {
            String str = "Sun, 11 Dec 2022 12:21:20 GMT";
            Date date = DateUtils.parseDate(str);
            ZonedDateTime zdt = date.toInstant().atZone(UTC);
            assertEquals(2022, zdt.getYear());
            assertEquals(12, zdt.getMonthValue());
            assertEquals(11, zdt.getDayOfMonth());
            assertEquals(12, zdt.getHour());
            assertEquals(21, zdt.getMinute());
            assertEquals(20, zdt.getSecond());
        }
    }

    @Test
    public void test1() {
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime zdt = ldt.atZone(UTC);
        System.out.println(zdt.format(formatter));
    }
}
