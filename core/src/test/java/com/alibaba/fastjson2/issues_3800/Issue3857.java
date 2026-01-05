package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Issue3857 {
    @Test
    public void testYear() {
        Year year = Year.of(2023);
        String json = JSON.toJSONString(year);
        assertEquals("\"2023\"", json);

        Year parsed = JSON.parseObject(json, Year.class);
        assertEquals(year, parsed);
    }

    @Test
    public void testYearInObject() {
        Bean bean = new Bean();
        bean.year = Year.of(2023);

        String json = JSON.toJSONString(bean);
        Bean parsed = JSON.parseObject(json, Bean.class);

        assertEquals(bean.year, parsed.year);
    }

    @Test
    public void testYearJSONB() {
        Year year = Year.of(2023);
        byte[] jsonbBytes = JSONB.toBytes(year);
        Year parsed = JSONB.parseObject(jsonbBytes, Year.class);
        assertEquals(year, parsed);
    }

    @Test
    public void testYearMonth() {
        YearMonth yearMonth = YearMonth.of(2023, 11);
        String json = JSON.toJSONString(yearMonth);
        assertEquals("\"2023-11\"", json);

        YearMonth parsed = JSON.parseObject(json, YearMonth.class);
        assertEquals(yearMonth, parsed);
    }

    @Test
    public void testYearMonthInObject() {
        Bean bean = new Bean();
        bean.yearMonth = YearMonth.of(2023, 11);

        String json = JSON.toJSONString(bean);
        Bean parsed = JSON.parseObject(json, Bean.class);

        assertEquals(bean.yearMonth, parsed.yearMonth);
    }

    @Test
    public void testYearMonthJSONB() {
        YearMonth yearMonth = YearMonth.of(2023, 11);
        byte[] jsonbBytes = JSONB.toBytes(yearMonth);
        YearMonth parsed = JSONB.parseObject(jsonbBytes, YearMonth.class);
        assertEquals(yearMonth, parsed);
    }

    @Test
    public void testMonthDay() {
        MonthDay monthDay = MonthDay.of(12, 25);
        String json = JSON.toJSONString(monthDay);
        assertEquals("\"--12-25\"", json);

        MonthDay parsed = JSON.parseObject(json, MonthDay.class);
        assertEquals(monthDay, parsed);
    }

    @Test
    public void testMonthDayInObject() {
        Bean bean = new Bean();
        bean.monthDay = MonthDay.of(12, 25);

        String json = JSON.toJSONString(bean);
        Bean parsed = JSON.parseObject(json, Bean.class);

        assertEquals(bean.monthDay, parsed.monthDay);
    }

    @Test
    public void testMonthDayJSONB() {
        MonthDay monthDay = MonthDay.of(12, 25);
        byte[] jsonbBytes = JSONB.toBytes(monthDay);
        MonthDay parsed = JSONB.parseObject(jsonbBytes, MonthDay.class);
        assertEquals(monthDay, parsed);
    }

    @Test
    public void testMonthDayLeapDay() {
        MonthDay leapDay = MonthDay.of(2, 29);
        String json = JSON.toJSONString(leapDay);
        assertEquals("\"--02-29\"", json);

        MonthDay parsed = JSON.parseObject(json, MonthDay.class);
        assertEquals(leapDay, parsed);
    }

    @Test
    public void testNullValues() {
        Year year = JSON.parseObject("null", Year.class);
        assertEquals(null, year);

        YearMonth yearMonth = JSON.parseObject("null", YearMonth.class);
        assertEquals(null, yearMonth);

        MonthDay monthDay = JSON.parseObject("null", MonthDay.class);
        assertEquals(null, monthDay);
    }

    @Test
    public void testAllTypesInBean() {
        Bean bean = new Bean();
        bean.year = Year.of(2023);
        bean.yearMonth = YearMonth.of(2023, 11);
        bean.monthDay = MonthDay.of(12, 25);

        String json = JSON.toJSONString(bean);
        assertNotNull(json);

        Bean parsed = JSON.parseObject(json, Bean.class);
        assertEquals(bean.year, parsed.year);
        assertEquals(bean.yearMonth, parsed.yearMonth);
        assertEquals(bean.monthDay, parsed.monthDay);
    }

    @Test
    public void testMonth() {
        Month month = Month.JANUARY;
        String json = JSON.toJSONString(month);
        assertEquals("\"JANUARY\"", json);

        Month parsed = JSON.parseObject(json, Month.class);
        assertEquals(month, parsed);
    }

    @Test
    public void testMonthInObject() {
        BeanWithEnum bean = new BeanWithEnum();
        bean.month = Month.DECEMBER;

        String json = JSON.toJSONString(bean);
        BeanWithEnum parsed = JSON.parseObject(json, BeanWithEnum.class);

        assertEquals(bean.month, parsed.month);
    }

    @Test
    public void testDayOfWeek() {
        DayOfWeek day = DayOfWeek.FRIDAY;
        String json = JSON.toJSONString(day);
        assertEquals("\"FRIDAY\"", json);

        DayOfWeek parsed = JSON.parseObject(json, DayOfWeek.class);
        assertEquals(day, parsed);
    }

    @Test
    public void testDayOfWeekInObject() {
        BeanWithEnum bean = new BeanWithEnum();
        bean.dayOfWeek = DayOfWeek.MONDAY;

        String json = JSON.toJSONString(bean);
        BeanWithEnum parsed = JSON.parseObject(json, BeanWithEnum.class);

        assertEquals(bean.dayOfWeek, parsed.dayOfWeek);
    }

    @Test
    public void testAllEnumsInBean() {
        BeanWithEnum bean = new BeanWithEnum();
        bean.month = Month.APRIL;
        bean.dayOfWeek = DayOfWeek.WEDNESDAY;

        String json = JSON.toJSONString(bean);
        assertNotNull(json);

        BeanWithEnum parsed = JSON.parseObject(json, BeanWithEnum.class);
        assertEquals(bean.month, parsed.month);
        assertEquals(bean.dayOfWeek, parsed.dayOfWeek);
    }

    public static class Bean {
        public Year year;
        public YearMonth yearMonth;
        public MonthDay monthDay;
    }

    public static class BeanWithEnum {
        public Month month;
        public DayOfWeek dayOfWeek;
    }
}
