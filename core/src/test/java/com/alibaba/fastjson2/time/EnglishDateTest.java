package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnglishDateTest {
    @Test
    public void test16() {
        String str = "\"3 Jun 2008 11:05\"";
        {
            Date date = JSON.parseObject(str, Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(0, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(0, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(0, zdt.getSecond());
        }
    }

    @Test
    public void test16Error() {
        String str = "\"3 AAA 2008 11:05\"";
        assertThrows(Exception.class, () -> JSON.parseObject(str, Date.class));
        assertThrows(Exception.class, () -> JSON.parseObject(str.toCharArray(), Date.class));
        assertThrows(Exception.class, () -> JSON.parseObject(str.getBytes(), Date.class));
    }

    @Test
    public void test19() {
        String str = "\"3 Jun 2008 11:05:30\"";
        {
            Date date = JSON.parseObject(str, Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
    }

    @Test
    public void test19Error() {
        String str = "\"3 AAA 2008 11:05:30\"";
        assertThrows(Exception.class, () -> JSON.parseObject(str, Date.class));
        assertThrows(Exception.class, () -> JSON.parseObject(str.toCharArray(), Date.class));
        assertThrows(Exception.class, () -> JSON.parseObject(str.getBytes(), Date.class));
    }

    @Test
    public void test20Error() {
        String str = "\"13 AAA 2008 11:05:30\"";
        assertThrows(Exception.class, () -> JSON.parseObject(str, Date.class));
        assertThrows(Exception.class, () -> JSON.parseObject(str.toCharArray(), Date.class));
        assertThrows(Exception.class, () -> JSON.parseObject(str.getBytes(), Date.class));
    }

    @Test
    public void test20() {
        String str = "\"13 Jun 2008 11:05:30\"";
        {
            Date date = JSON.parseObject(str, Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }
    }

    @Test
    public void test17Error() {
        String str = "\"13 AAA 2008 11:05\"";
        assertThrows(Exception.class, () -> JSON.parseObject(str, Date.class));
        assertThrows(Exception.class, () -> JSON.parseObject(str.toCharArray(), Date.class));
        assertThrows(Exception.class, () -> JSON.parseObject(str.getBytes(), Date.class));
    }

    @Test
    public void test17() {
        String str = "\"13 Jun 2008 11:05\"";
        {
            Date date = JSON.parseObject(str, Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(0, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(0, zdt.getSecond());
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(0, zdt.getSecond());
        }
    }

    @Test
    public void parseDate() {
        {
            String str = "3 Jun 2008 11:05:30";
            Date date = DateUtils.parseDate(str);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }

        {
            String str = "13 Jun 2008 11:05:30";
            Date date = DateUtils.parseDate(str);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(30, zdt.getSecond());
        }

        {
            String str = "3 Jun 2008 11:05";
            Date date = DateUtils.parseDate(str);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(3, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(0, zdt.getSecond());
        }
        {
            String str = "13 Jun 2008 11:05";
            Date date = DateUtils.parseDate(str);
            ZonedDateTime zdt = date.toInstant().atZone(IOUtils.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.getYear());
            assertEquals(6, zdt.getMonthValue());
            assertEquals(13, zdt.getDayOfMonth());
            assertEquals(11, zdt.getHour());
            assertEquals(5, zdt.getMinute());
            assertEquals(0, zdt.getSecond());
        }
    }

    @Test
    public void parseLocalDateTime() {
        {
            String str = "3 Jun 2008 11:05:30";
            LocalDateTime ldt = DateUtils.parseLocalDateTime(str);
            assertEquals(2008, ldt.getYear());
            assertEquals(6, ldt.getMonthValue());
            assertEquals(3, ldt.getDayOfMonth());
            assertEquals(11, ldt.getHour());
            assertEquals(5, ldt.getMinute());
            assertEquals(30, ldt.getSecond());
        }

        {
            String str = "13 Jun 2008 11:05:30";
            LocalDateTime ldt = DateUtils.parseLocalDateTime(str);
            assertEquals(2008, ldt.getYear());
            assertEquals(6, ldt.getMonthValue());
            assertEquals(13, ldt.getDayOfMonth());
            assertEquals(11, ldt.getHour());
            assertEquals(5, ldt.getMinute());
            assertEquals(30, ldt.getSecond());
        }

        {
            String str = "3 Jun 2008 11:05";
            LocalDateTime ldt = DateUtils.parseLocalDateTime(str);
            assertEquals(2008, ldt.getYear());
            assertEquals(6, ldt.getMonthValue());
            assertEquals(3, ldt.getDayOfMonth());
            assertEquals(11, ldt.getHour());
            assertEquals(5, ldt.getMinute());
            assertEquals(0, ldt.getSecond());
        }
        {
            String str = "13 Jun 2008 11:05";
            LocalDateTime ldt = DateUtils.parseLocalDateTime(str);
            assertEquals(2008, ldt.getYear());
            assertEquals(6, ldt.getMonthValue());
            assertEquals(13, ldt.getDayOfMonth());
            assertEquals(11, ldt.getHour());
            assertEquals(5, ldt.getMinute());
            assertEquals(0, ldt.getSecond());
        }
    }

    @Test
    public void parseLocalDateTimeError() {
        assertThrows(Exception.class, () -> DateUtils.parseLocalDateTime("3 AAA 2008 11:05:30"));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDateTime("13 AAA 2008 11:05"));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDateTime("3 AAA 2008 11:05"));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDateTime("13 AAA 2008 11:05:30"));
    }

    @Test
    public void parseDateError() {
        assertThrows(Exception.class, () -> DateUtils.parseDate("3 AAA 2008 11:05"));
        assertThrows(Exception.class, () -> DateUtils.parseDate("3 AAA 2008 11:05:30"));
        assertThrows(Exception.class, () -> DateUtils.parseDate("13 AAA 2008 11:05"));
        assertThrows(Exception.class, () -> DateUtils.parseDate("13 AAA 2008 11:05:30"));
    }

    @Test
    public void parseLocalDate() {
        {
            String str = "3 Jun 2008";
            LocalDate localDate = DateUtils.parseLocalDate(str);
            assertEquals(2008, localDate.getYear());
            assertEquals(6, localDate.getMonthValue());
            assertEquals(3, localDate.getDayOfMonth());
        }

        {
            String str = "13 Jun 2008";
            LocalDate localDate = DateUtils.parseLocalDate(str);
            assertEquals(2008, localDate.getYear());
            assertEquals(6, localDate.getMonthValue());
            assertEquals(13, localDate.getDayOfMonth());
        }
    }

    @Test
    public void parseLocalDateError() {
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("3 AAA 2008"));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("13 AAA 2008"));
    }
}
