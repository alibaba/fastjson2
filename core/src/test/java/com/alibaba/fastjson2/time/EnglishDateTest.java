package com.alibaba.fastjson2.time;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.alibaba.fastjson2.time.ZoneId.DEFAULT_ZONE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnglishDateTest {
    @Test
    public void test16() {
        String str = "\"3 Jun 2008 11:05\"";
        {
            Date date = JSON.parseObject(str, Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
        }
        {
            byte[] bytes = JSONB.toBytes(JSON.parse(str));
            Date date = JSONB.parseObject(bytes, Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
        }
        {
            byte[] bytes = JSONB.toBytes(JSON.parse(str));
            Date date = JSONB.parseObject(bytes, Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
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
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
        }
        {
            Date date = JSON.parseObject(str.toCharArray(), Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
        }
        {
            Date date = JSON.parseObject(str.getBytes(), Date.class);
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
        }
    }

    @Test
    public void parseDate() {
        {
            String str = "3 Jun 2008 11:05:30";
            Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
        }

        {
            String str = "13 Jun 2008 11:05:30";
            Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(30, zdt.dateTime.time.second);
        }

        {
            String str = "3 Jun 2008 11:05";
            Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(3, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
        }
        {
            String str = "13 Jun 2008 11:05";
            Date date = new Date(DateUtils.parseMillis(str, DEFAULT_ZONE_ID));
            ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.of(date), ZoneId.SHANGHAI_ZONE_ID);
            assertEquals(2008, zdt.dateTime.date.year);
            assertEquals(6, zdt.dateTime.date.monthValue);
            assertEquals(13, zdt.dateTime.date.dayOfMonth);
            assertEquals(11, zdt.dateTime.time.hour);
            assertEquals(5, zdt.dateTime.time.minute);
            assertEquals(0, zdt.dateTime.time.second);
        }
    }

    @Test
    public void parseLocalDateTime() {
        {
            String str = "3 Jun 2008 11:05:30";
            LocalDateTime ldt = DateUtils.parseLocalDateTime(str, 0, str.length());
            assertEquals(2008, ldt.date.year);
            assertEquals(6, ldt.date.monthValue);
            assertEquals(3, ldt.date.dayOfMonth);
            assertEquals(11, ldt.time.hour);
            assertEquals(5, ldt.time.minute);
            assertEquals(30, ldt.time.second);

            byte[] bytes = str.getBytes();
            assertEquals(ldt, DateUtils.parseLocalDateTime(bytes, 0, bytes.length));

            char[] chars = str.toCharArray();
            assertEquals(ldt, DateUtils.parseLocalDateTime(chars, 0, bytes.length));
        }

        {
            String str = "13 Jun 2008 11:05:30";
            LocalDateTime ldt = DateUtils.parseLocalDateTime(str, 0, str.length());
            assertEquals(2008, ldt.date.year);
            assertEquals(6, ldt.date.monthValue);
            assertEquals(13, ldt.date.dayOfMonth);
            assertEquals(11, ldt.time.hour);
            assertEquals(5, ldt.time.minute);
            assertEquals(30, ldt.time.second);

            byte[] bytes = str.getBytes();
            assertEquals(ldt, DateUtils.parseLocalDateTime(bytes, 0, bytes.length));

            char[] chars = str.toCharArray();
            assertEquals(ldt, DateUtils.parseLocalDateTime(chars, 0, bytes.length));
        }

        {
            String str = "3 Jun 2008 11:05";
            LocalDateTime ldt = DateUtils.parseLocalDateTime(str, 0, str.length());
            assertEquals(2008, ldt.date.year);
            assertEquals(6, ldt.date.monthValue);
            assertEquals(3, ldt.date.dayOfMonth);
            assertEquals(11, ldt.time.hour);
            assertEquals(5, ldt.time.minute);
            assertEquals(0, ldt.time.second);

            byte[] bytes = str.getBytes();
            assertEquals(ldt, DateUtils.parseLocalDateTime(bytes, 0, bytes.length));

            char[] chars = str.toCharArray();
            assertEquals(ldt, DateUtils.parseLocalDateTime(chars, 0, bytes.length));
        }
        {
            String str = "13 Jun 2008 11:05";
            LocalDateTime ldt = DateUtils.parseLocalDateTime(str, 0, str.length());
            assertEquals(2008, ldt.date.year);
            assertEquals(6, ldt.date.monthValue);
            assertEquals(13, ldt.date.dayOfMonth);
            assertEquals(11, ldt.time.hour);
            assertEquals(5, ldt.time.minute);
            assertEquals(0, ldt.time.second);

            byte[] bytes = str.getBytes();
            assertEquals(ldt, DateUtils.parseLocalDateTime(bytes, 0, bytes.length));

            char[] chars = str.toCharArray();
            assertEquals(ldt, DateUtils.parseLocalDateTime(chars, 0, bytes.length));
        }
    }

    @Test
    public void parseLocalDateTimeError() {
        assertThrows(Exception.class, () -> parseLocalDateTime("3 AAA 2008 11:05:30"));
        assertThrows(Exception.class, () -> parseLocalDateTime("13 AAA 2008 11:05"));
        assertThrows(Exception.class, () -> parseLocalDateTime("3 AAA 2008 11:05"));
        assertThrows(Exception.class, () -> parseLocalDateTime("13 AAA 2008 11:05:30"));
    }

    public static LocalDateTime parseLocalDateTime(String str) {
        if (str == null) {
            return null;
        }
        return DateUtils.parseLocalDateTime(str, 0, str.length());
    }

    @Test
    public void parseDateError() {
        assertThrows(Exception.class, () -> DateUtils.parseMillis("3 AAA 2008 11:05", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseMillis("3 AAA 2008 11:05:30", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseMillis("13 AAA 2008 11:05", DEFAULT_ZONE_ID));
        assertThrows(Exception.class, () -> DateUtils.parseMillis("13 AAA 2008 11:05:30", DEFAULT_ZONE_ID));
    }

    @Test
    public void parseLocalDate() {
        {
            String str = "3 Jun 2008";
            LocalDate localDate = DateUtils.parseLocalDate(str);
            assertEquals(2008, localDate.year);
            assertEquals(6, localDate.monthValue);
            assertEquals(3, localDate.dayOfMonth);
        }

        {
            String str = "13 Jun 2008";
            LocalDate localDate = DateUtils.parseLocalDate(str);
            assertEquals(2008, localDate.year);
            assertEquals(6, localDate.monthValue);
            assertEquals(13, localDate.dayOfMonth);
        }
    }

    @Test
    public void parseLocalDateError() {
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("3 AAA 2008"));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("13 AAA 2008"));
    }
}
