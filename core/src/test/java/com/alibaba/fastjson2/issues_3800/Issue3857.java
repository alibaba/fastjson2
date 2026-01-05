package com.alibaba.fastjson2.issues_3800;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;

import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3857 {
    @Test
    public void testYearVariants() {
        Year parsed = JSON.parseObject("\"+2023\"", Year.class);
        assertEquals(Year.of(2023), parsed);

        parsed = JSON.parseObject("2023", Year.class);
        assertEquals(Year.of(2023), parsed);
    }

    @Test
    public void testYearMonthVariants() {
        YearMonth parsed = JSON.parseObject("\"202311\"", YearMonth.class);
        assertEquals(YearMonth.of(2023, 11), parsed);

        parsed = JSON.parseObject("\"+2023-11\"", YearMonth.class);
        assertEquals(YearMonth.of(2023, 11), parsed);

        parsed = JSON.parseObject("202311", YearMonth.class);
        assertEquals(YearMonth.of(2023, 11), parsed);
    }

    @Test
    public void testMonthDayVariants() {
        MonthDay parsed = JSON.parseObject("\"12-25\"", MonthDay.class);
        assertEquals(MonthDay.of(12, 25), parsed);

        parsed = JSON.parseObject("1225", MonthDay.class);
        assertEquals(MonthDay.of(12, 25), parsed);
    }
}
