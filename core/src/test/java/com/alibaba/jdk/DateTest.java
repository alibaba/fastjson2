package com.alibaba.jdk;

import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTest {
    @Test
    public void year() {
        Exception e0 = null, e1 = null;
        int year = Year.MAX_VALUE + 1;
        int month = 2;
        int dayOfMonth = 1;
        try {
            LocalDate.of(year, month, dayOfMonth);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            validate(year, month, dayOfMonth);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    @Test
    public void month() {
        Exception e0 = null, e1 = null;
        int year = 1979;
        int month = 13;
        int dayOfMonth = 1;
        try {
            LocalDate.of(year, month, dayOfMonth);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            validate(year, month, dayOfMonth);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    @Test
    public void dayOfMonth() {
        Exception e0 = null, e1 = null;
        int year = 1979;
        int month = 1;
        int dayOfMonth = 32;
        try {
            LocalDate.of(year, month, dayOfMonth);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            validate(year, month, dayOfMonth);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    @Test
    public void dayOfMonth2() {
        Exception e0 = null, e1 = null;
        int year = 1979;
        int month = 2;
        int dayOfMonth = -1;
        try {
            LocalDate.of(year, month, dayOfMonth);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            validate(year, month, dayOfMonth);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    @Test
    public void dayOfYear() {
        Exception e0 = null, e1 = null;
        int year = 1979;
        int dayOfYear = 366;
        try {
            LocalDate.ofYearDay(year, dayOfYear);
        } catch (Exception e) {
            e0 = e;
        }

        try {
            validate(year, dayOfYear);
        } catch (Exception e) {
            e1 = e;
        }

        assertEquals(e0.getMessage(), e1.getMessage());
    }

    public void validate(int year, int month, int dayOfMonth) {
        if (year < Year.MIN_VALUE || year > Year.MAX_VALUE) {
            throw new DateTimeException("Invalid value for Year (valid values " + Year.MIN_VALUE + " - " + Year.MAX_VALUE + "): " + year);
        }

        if (month < 1 || month > 12) {
            throw new DateTimeException("Invalid value for MonthOfYear (valid values 1 - 12): " + month);
        }

        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new DateTimeException("Invalid value for DayOfMonth (valid values 1 - 28/31): " + dayOfMonth);
        }
    }

    public void validate(int year, int dayOfYear) {
        if (year < Year.MIN_VALUE || year > Year.MAX_VALUE) {
            throw new DateTimeException("Invalid value for Year (valid values " + Year.MIN_VALUE + " - " + Year.MAX_VALUE + "): " + year);
        }

        if (dayOfYear < 1 || dayOfYear > 366) {
            throw new DateTimeException("Invalid value for DayOfYear (valid values 1 - 365/366): " + dayOfYear);
        }
    }
}
