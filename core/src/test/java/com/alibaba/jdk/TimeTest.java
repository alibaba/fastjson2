package com.alibaba.jdk;

import com.alibaba.fastjson2.time.DateTimeException;
import com.alibaba.fastjson2.time.LocalTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeTest {
    @Test
    public void t3_hour() {
        Exception e0 = null, e1 = null;
        int hour = 24;
        int minute = 1;
        int second = 1;
        try {
            LocalTime.of(hour, minute, second);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            v3(hour, minute, second);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    @Test
    public void t3_minute() {
        Exception e0 = null, e1 = null;
        int hour = 1;
        int minute = 60;
        int second = 1;
        try {
            LocalTime.of(hour, minute, second);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            v3(hour, minute, second);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    @Test
    public void t3_second() {
        Exception e0 = null, e1 = null;
        int hour = 1;
        int minute = 1;
        int second = 60;
        try {
            LocalTime.of(hour, minute, second);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            v3(hour, minute, second);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    @Test
    public void t4_nanoOfSecond() {
        Exception e0 = null, e1 = null;
        int hour = 1;
        int minute = 1;
        int second = 1;
        int nanoOfSecond = 999999999 + 1;
        try {
            LocalTime.of(hour, minute, second, nanoOfSecond);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            v4(hour, minute, second, nanoOfSecond);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    @Test
    public void t1_ofNanoOfDay() {
        Exception e0 = null, e1 = null;

        long nanoOfDay = 86400L * 1000_000_000L;
        try {
            LocalTime.ofNanoOfDay(nanoOfDay);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            ofNanoOfDay(nanoOfDay);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    public void v3(int hour, int minute, int second) {
        if (hour < 0 || hour > 23) {
            throw new DateTimeException("Invalid value for HourOfDay (valid values 0 - 23): " + hour);
        }

        if (minute < 1 || minute > 12) {
            throw new DateTimeException("Invalid value for MinuteOfHour (valid values 0 - 59): " + minute);
        }

        if (second < 1 || second > 31) {
            throw new DateTimeException("Invalid value for SecondOfMinute (valid values 0 - 59): " + second);
        }
    }

    public void secondOfDay(long secondOfDay) {
        if (secondOfDay < 1 || secondOfDay > 86399) {
            throw new DateTimeException("Invalid value for SecondOfDay (valid values 0 - 86399): " + secondOfDay);
        }
    }

    public void ofNanoOfDay(long nanoOfDay) {
        if (nanoOfDay < 1 || nanoOfDay > 86399999999999L) {
            throw new DateTimeException("Invalid value for NanoOfDay (valid values 0 - 86399999999999): " + nanoOfDay);
        }
    }

    public void v4(int hour, int minute, int second, int nanoOfSecond) {
        if (hour < 0 || hour > 23) {
            throw new DateTimeException("Invalid value for HourOfDay (valid values 0 - 23): " + hour);
        }

        if (minute < 1 || minute > 59) {
            throw new DateTimeException("Invalid value for MinuteOfHour (valid values 0 - 59): " + minute);
        }

        if (second < 1 || second > 59) {
            throw new DateTimeException("Invalid value for SecondOfMinute (valid values 0 - 59): " + second);
        }

        if (nanoOfSecond < 1 || nanoOfSecond > 999_999_999) {
            throw new DateTimeException("Invalid value for NanoOfSecond (valid values 0 - 999999999): " + nanoOfSecond);
        }
    }
}
