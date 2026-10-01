package com.alibaba.fastjson2.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateUtilsReviewRegressionTest {
    @Test
    public void slicedLocalTimesAndDates() {
        String time = "xx12:34:56";
        LocalDateTime expected = LocalDateTime.of(1970, 1, 1, 12, 34, 56);
        assertEquals(expected, DateUtils.parseLocalDateTime(time.toCharArray(), 2, 8));
        assertEquals(expected, DateUtils.parseLocalDateTime(time.getBytes(StandardCharsets.US_ASCII), 2, 8));
        assertEquals(LocalDateTime.of(2024, 1, 2, 3, 4, 5),
                DateUtils.parseLocalDateTime19("中2024-01-02 03:04:05", 1));
        String invalid = "xx202a01020304";
        assertThrows(DateTimeParseException.class,
                () -> DateUtils.parseLocalDateTime12(invalid.toCharArray(), 2));
        assertThrows(DateTimeParseException.class,
                () -> DateUtils.parseLocalDateTime12(invalid.getBytes(StandardCharsets.US_ASCII), 2));
        String truncated = "12 Jan 2024 12:34:5";
        assertNull(DateUtils.parseLocalDateTime20(truncated.toCharArray(), 0));
        assertNull(DateUtils.parseLocalDateTime20(truncated.getBytes(StandardCharsets.US_ASCII), 0));
    }

    @Test
    public void slicedMillisUseSuffixAndZone() {
        for (String value : new String[]{"2024-01-02Z", "\"2024-01-02 03:04:05\"", "\"null\""}) {
            long expected = "\"null\"".equals(value) ? 0
                    : (value.endsWith("Z") ? LocalDateTime.of(2024, 1, 2, 0, 0).toInstant(ZoneOffset.UTC)
                    : LocalDateTime.of(2024, 1, 2, 3, 4, 5).toInstant(ZoneOffset.ofHours(3))).toEpochMilli();
            String input = "xx" + value + "tail";
            assertEquals(expected, DateUtils.parseMillis(input.toCharArray(), 2, value.length(), ZoneOffset.ofHours(3)));
            assertEquals(expected, DateUtils.parseMillis(input.getBytes(StandardCharsets.US_ASCII),
                    2, value.length(), StandardCharsets.US_ASCII, ZoneOffset.ofHours(3)));
        }
    }

    @Test
    public void midnightAndTimeZoneNames() {
        for (String value : new String[]{"Jan 2, 2024 12:34:56 AM", "01/02/2024 12:34:56 AM"}) {
            ZonedDateTime expected = LocalDateTime.of(2024, 1, 2, 0, 34, 56).atZone(ZoneOffset.UTC);
            assertEquals(expected, DateUtils.parseZonedDateTime(value.toCharArray(), 0, value.length(), ZoneOffset.UTC));
            assertEquals(expected, DateUtils.parseZonedDateTime(value.getBytes(StandardCharsets.US_ASCII),
                    0, value.length(), ZoneOffset.UTC));
            assertEquals(12, DateUtils.parseZonedDateTime(value.replace("AM", "PM"), ZoneOffset.UTC).getHour());
        }
        String value = "Tue, 02 Jan 2024 03:04:05 EST";
        String input = "xx" + value;
        assertEquals(ZoneOffset.ofHours(-5),
                DateUtils.parseZonedDateTime(input.toCharArray(), 2, value.length(), ZoneOffset.UTC).getOffset());
        assertEquals(ZoneOffset.ofHours(-5), DateUtils.parseZonedDateTime(input.getBytes(StandardCharsets.US_ASCII),
                2, value.length(), ZoneOffset.UTC).getOffset());
    }

    @Test
    public void epochDayHandlesNegativeAndLargeYears() {
        for (int year : new int[]{-400, -100, -4, -1, 0, 2000, 6000000}) {
            LocalDateTime date = LocalDateTime.of(year, 3, 1, 1, 2, 3);
            assertEquals(date.toEpochSecond(ZoneOffset.UTC), DateUtils.utcSeconds(year, 3, 1, 1, 2, 3));
            assertEquals(date.toInstant(ZoneOffset.UTC).toEpochMilli(), DateUtils.millis(date, ZoneOffset.UTC));
        }
    }

    @Test
    public void validatorsRejectInvalidDatesAndTimes() {
        for (String value : new String[]{"abcd-01-01", "2024-00-01", "2024-01-00", "2024-0:-01"}) {
            assertFalse(DateUtils.isLocalDate(value), value);
        }
        for (String value : new String[]{"24:00:00", "12:60:00", "12:34:60"}) {
            assertFalse(DateUtils.isLocalTime(value), value);
            assertFalse(DateUtils.isDate("2024-01-02 " + value), value);
        }
        assertFalse(DateUtils.isDate("2024-00-01 12:34:56"));
        assertTrue(DateUtils.isLocalDate("2000-02-29"));
        assertFalse(DateUtils.isLocalDate("1900-02-29"));
        assertTrue(DateUtils.isLocalTime("23:59:59"));
    }

    @Test
    public void formattingPreservesSeparatorAndNegativeSubHourOffset() {
        assertEquals("2024/01/02 03:04:05", DateUtils.format(2024, 1, 2, 3, 4, 5,
                DateUtils.DateTimeFormatPattern.DATE_TIME_FORMAT_19_SLASH));
        assertEquals("1969-12-31 23:30:00-00:30", DateUtils.toString(0, true, ZoneOffset.ofHoursMinutes(0, -30)));
    }
}
