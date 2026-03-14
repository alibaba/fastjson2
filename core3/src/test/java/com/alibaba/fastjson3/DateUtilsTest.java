package com.alibaba.fastjson3;

import com.alibaba.fastjson3.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for DateUtils high-performance date parsing and writing.
 */
class DateUtilsTest {
    // ---- LocalDate parsing ----
    @Test
    void parseLocalDate_dashFormat() {
        assertEquals(LocalDate.of(2024, 6, 15), DateUtils.parseLocalDate("2024-06-15"));
    }

    @Test
    void parseLocalDate_slashFormat() {
        assertEquals(LocalDate.of(2024, 6, 15), DateUtils.parseLocalDate("2024/06/15"));
    }

    @Test
    void parseLocalDate_dotFormat() {
        assertEquals(LocalDate.of(2024, 6, 15), DateUtils.parseLocalDate("15.06.2024"));
    }

    @Test
    void parseLocalDate_compactFormat() {
        assertEquals(LocalDate.of(2024, 6, 15), DateUtils.parseLocalDate("20240615"));
    }

    @Test
    void parseLocalDate_null() {
        assertNull(DateUtils.parseLocalDate(null));
        assertNull(DateUtils.parseLocalDate(""));
    }

    @Test
    void parseLocalDate_leapYear() {
        assertEquals(LocalDate.of(2024, 2, 29), DateUtils.parseLocalDate("2024-02-29"));
        assertThrows(Exception.class, () -> DateUtils.parseLocalDate("2023-02-29"));
    }

    @Test
    void parseLocalDate_boundaries() {
        assertEquals(LocalDate.of(2000, 1, 1), DateUtils.parseLocalDate("2000-01-01"));
        assertEquals(LocalDate.of(2099, 12, 31), DateUtils.parseLocalDate("2099-12-31"));
    }

    // ---- LocalTime parsing ----

    @Test
    void parseLocalTime_hms() {
        assertEquals(LocalTime.of(10, 30, 45), DateUtils.parseLocalTime("10:30:45"));
    }

    @Test
    void parseLocalTime_hm() {
        assertEquals(LocalTime.of(10, 30), DateUtils.parseLocalTime("10:30"));
    }

    @Test
    void parseLocalTime_withMillis() {
        assertEquals(LocalTime.of(10, 30, 45, 123_000_000), DateUtils.parseLocalTime("10:30:45.123"));
    }

    @Test
    void parseLocalTime_withNanos() {
        assertEquals(LocalTime.of(10, 30, 45, 123_456_789), DateUtils.parseLocalTime("10:30:45.123456789"));
    }

    @Test
    void parseLocalTime_null() {
        assertNull(DateUtils.parseLocalTime(null));
        assertNull(DateUtils.parseLocalTime(""));
    }

    // ---- LocalDateTime parsing ----

    @Test
    void parseLocalDateTime_dashSpace() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45),
                DateUtils.parseLocalDateTime("2024-06-15 10:30:45")
        );
    }

    @Test
    void parseLocalDateTime_dashT() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45),
                DateUtils.parseLocalDateTime("2024-06-15T10:30:45")
        );
    }

    @Test
    void parseLocalDateTime_slashSpace() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45),
                DateUtils.parseLocalDateTime("2024/06/15 10:30:45")
        );
    }

    @Test
    void parseLocalDateTime_dotFormat() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45),
                DateUtils.parseLocalDateTime("15.06.2024 10:30:45")
        );
    }

    @Test
    void parseLocalDateTime_ddSlash() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45),
                DateUtils.parseLocalDateTime("15/06/2024 10:30:45")
        );
    }

    @Test
    void parseLocalDateTime_16chars() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30),
                DateUtils.parseLocalDateTime("2024-06-15 10:30")
        );
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30),
                DateUtils.parseLocalDateTime("2024-06-15T10:30")
        );
    }

    @Test
    void parseLocalDateTime_14chars() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45),
                DateUtils.parseLocalDateTime("20240615103045")
        );
    }

    @Test
    void parseLocalDateTime_withMillis() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45, 123_000_000),
                DateUtils.parseLocalDateTime("2024-06-15T10:30:45.123")
        );
    }

    @Test
    void parseLocalDateTime_withNanos() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45, 123_456_789),
                DateUtils.parseLocalDateTime("2024-06-15T10:30:45.123456789")
        );
    }

    @Test
    void parseLocalDateTime_withTimezoneIgnored() {
        // For LocalDateTime, timezone suffix is ignored
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45),
                DateUtils.parseLocalDateTime("2024-06-15T10:30:45Z")
        );
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 10, 30, 45),
                DateUtils.parseLocalDateTime("2024-06-15T10:30:45+08:00")
        );
    }

    @Test
    void parseLocalDateTime_dateOnly10() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 0, 0),
                DateUtils.parseLocalDateTime("2024-06-15")
        );
    }

    @Test
    void parseLocalDateTime_dateOnly8() {
        assertEquals(
                LocalDateTime.of(2024, 6, 15, 0, 0),
                DateUtils.parseLocalDateTime("20240615")
        );
    }

    @Test
    void parseLocalDateTime_null() {
        assertNull(DateUtils.parseLocalDateTime(null));
        assertNull(DateUtils.parseLocalDateTime(""));
    }

    // ---- Instant parsing ----

    @Test
    void parseInstant_isoZ() {
        assertEquals(
                Instant.parse("2024-06-15T10:30:00Z"),
                DateUtils.parseInstant("2024-06-15T10:30:00Z")
        );
    }

    @Test
    void parseInstant_isoWithMillisZ() {
        assertEquals(
                Instant.parse("2024-06-15T10:30:00.123Z"),
                DateUtils.parseInstant("2024-06-15T10:30:00.123Z")
        );
    }

    @Test
    void parseInstant_isoWithOffset() {
        Instant expected = LocalDateTime.of(2024, 6, 15, 10, 30, 0)
                .toInstant(ZoneOffset.ofHours(8));
        assertEquals(expected, DateUtils.parseInstant("2024-06-15T10:30:00+08:00"));
    }

    @Test
    void parseInstant_19charsDefaultZone() {
        Instant expected = LocalDateTime.of(2024, 6, 15, 10, 30, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant();
        assertEquals(expected, DateUtils.parseInstant("2024-06-15T10:30:00"));
        assertEquals(expected, DateUtils.parseInstant("2024-06-15 10:30:00"));
    }

    @Test
    void parseInstant_null() {
        assertNull(DateUtils.parseInstant(null));
        assertNull(DateUtils.parseInstant(""));
    }

    // ---- OffsetDateTime parsing ----

    @Test
    void parseOffsetDateTime_isoZ() {
        assertEquals(
                OffsetDateTime.of(2024, 6, 15, 10, 30, 0, 0, ZoneOffset.UTC),
                DateUtils.parseOffsetDateTime("2024-06-15T10:30:00Z")
        );
    }

    @Test
    void parseOffsetDateTime_isoWithOffset() {
        assertEquals(
                OffsetDateTime.of(2024, 6, 15, 10, 30, 0, 0, ZoneOffset.ofHours(8)),
                DateUtils.parseOffsetDateTime("2024-06-15T10:30:00+08:00")
        );
    }

    // ---- Date parsing ----

    @Test
    void parseDate_isoInstant() {
        Date expected = Date.from(Instant.parse("2024-06-15T10:30:00Z"));
        assertEquals(expected, DateUtils.parseDate("2024-06-15T10:30:00Z"));
    }

    @Test
    void parseDate_millis() {
        long millis = 1718444400000L;
        assertEquals(new Date(millis), DateUtils.parseDate(String.valueOf(millis)));
    }

    @Test
    void parseDate_dateOnly() {
        LocalDate ld = LocalDate.of(2024, 6, 15);
        Date expected = Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertEquals(expected, DateUtils.parseDate("2024-06-15"));
    }

    @Test
    void parseDate_null() {
        assertNull(DateUtils.parseDate(null));
        assertNull(DateUtils.parseDate(""));
    }

    // ---- Date writing ----

    @Test
    void writeLocalDate() {
        byte[] buf = new byte[10];
        int len = DateUtils.writeLocalDate(buf, 0, LocalDate.of(2024, 6, 15));
        assertEquals(10, len);
        assertEquals("2024-06-15", new String(buf, 0, len));
    }

    @Test
    void writeLocalDateTime() {
        byte[] buf = new byte[30];
        int len = DateUtils.writeLocalDateTime(buf, 0, LocalDateTime.of(2024, 6, 15, 10, 30, 45));
        assertEquals(19, len);
        assertEquals("2024-06-15T10:30:45", new String(buf, 0, len));
    }

    @Test
    void writeLocalDateTimeWithNanos() {
        byte[] buf = new byte[40];
        int len = DateUtils.writeLocalDateTime(buf, 0,
                LocalDateTime.of(2024, 6, 15, 10, 30, 45, 123_000_000));
        assertEquals("2024-06-15T10:30:45.123", new String(buf, 0, len));
    }

    @Test
    void writeLocalTime() {
        byte[] buf = new byte[8];
        int len = DateUtils.writeLocalTime(buf, 0, LocalTime.of(10, 30, 45));
        assertEquals(8, len);
        assertEquals("10:30:45", new String(buf, 0, len));
    }

    // ---- Integration: JSON serialization/deserialization ----

    public static class DateBean {
        public String name;
        public LocalDate date;
        public LocalDateTime dateTime;
        public LocalTime time;
        public Instant instant;
        public Date legacyDate;

        public DateBean() {
        }
    }

    @Test
    void jsonRoundTrip_dashFormat() {
        String json = "{\"name\":\"test\",\"dateTime\":\"2024-06-15 10:30:45\"}";
        DateBean bean = ObjectMapper.shared().readValue(json, DateBean.class);
        assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30, 45), bean.dateTime);
    }

    @Test
    void jsonRoundTrip_slashFormat() {
        String json = "{\"name\":\"test\",\"dateTime\":\"2024/06/15 10:30:45\"}";
        DateBean bean = ObjectMapper.shared().readValue(json, DateBean.class);
        assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30, 45), bean.dateTime);
    }

    @Test
    void jsonRoundTrip_compactDate() {
        String json = "{\"name\":\"test\",\"date\":\"20240615\"}";
        DateBean bean = ObjectMapper.shared().readValue(json, DateBean.class);
        assertEquals(LocalDate.of(2024, 6, 15), bean.date);
    }

    @Test
    void jsonRoundTrip_isoInstant() {
        String json = "{\"name\":\"test\",\"instant\":\"2024-06-15T10:30:00Z\"}";
        DateBean bean = ObjectMapper.shared().readValue(json, DateBean.class);
        assertEquals(Instant.parse("2024-06-15T10:30:00Z"), bean.instant);
    }

    @Test
    void jsonSerialize_localDate() {
        DateBean bean = new DateBean();
        bean.name = "test";
        bean.date = LocalDate.of(2024, 6, 15);
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"2024-06-15\""), json);
    }

    @Test
    void jsonSerialize_localDateTime() {
        DateBean bean = new DateBean();
        bean.name = "test";
        bean.dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"2024-06-15T10:30:45\""), json);
    }

    @Test
    void jsonSerialize_instant() {
        DateBean bean = new DateBean();
        bean.name = "test";
        bean.instant = Instant.parse("2024-06-15T10:30:00Z");
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"2024-06-15T10:30:00Z\""), json);
    }

    @Test
    void jsonSerialize_localDateTimeWithNanos() {
        DateBean bean = new DateBean();
        bean.name = "test";
        bean.dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45, 123_456_000);
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("\"2024-06-15T10:30:45.123456\""), json);
    }

    @Test
    void jsonRoundTrip_full() {
        DateBean bean = new DateBean();
        bean.name = "test";
        bean.date = LocalDate.of(2024, 6, 15);
        bean.dateTime = LocalDateTime.of(2024, 6, 15, 10, 30, 45);
        bean.time = LocalTime.of(10, 30, 45);
        bean.instant = Instant.parse("2024-06-15T10:30:00Z");

        String json = JSON.toJSONString(bean);
        DateBean deserialized = ObjectMapper.shared().readValue(json, DateBean.class);
        assertEquals(bean.name, deserialized.name);
        assertEquals(bean.date, deserialized.date);
        assertEquals(bean.dateTime, deserialized.dateTime);
        assertEquals(bean.time, deserialized.time);
        assertEquals(bean.instant, deserialized.instant);
    }

    @Test
    void jsonRoundTrip_legacyDate() {
        DateBean bean = new DateBean();
        bean.name = "test";
        bean.legacyDate = Date.from(Instant.parse("2024-06-15T10:30:00Z"));
        String json = JSON.toJSONString(bean);
        assertTrue(json.contains("2024-06-15T10:30:00Z"), json);

        DateBean deserialized = ObjectMapper.shared().readValue(json, DateBean.class);
        assertEquals(bean.legacyDate, deserialized.legacyDate);
    }

    // ---- Edge cases ----

    @Test
    void parseLocalDateTime_midnight() {
        assertEquals(
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                DateUtils.parseLocalDateTime("2024-01-01 00:00:00")
        );
    }

    @Test
    void parseLocalDateTime_endOfDay() {
        assertEquals(
                LocalDateTime.of(2024, 12, 31, 23, 59, 59),
                DateUtils.parseLocalDateTime("2024-12-31 23:59:59")
        );
    }

    @Test
    void parseInstant_withNanos() {
        Instant expected = Instant.parse("2024-06-15T10:30:00.123456789Z");
        assertEquals(expected, DateUtils.parseInstant("2024-06-15T10:30:00.123456789Z"));
    }

    @Test
    void toEpochMilli_epoch() {
        assertEquals(0, DateUtils.toEpochMilli(1970, 1, 1, 0, 0, 0, 0, 0));
    }

    @Test
    void toEpochMilli_knownDate() {
        long expected = LocalDateTime.of(2024, 6, 15, 10, 30, 0)
                .toInstant(ZoneOffset.UTC).toEpochMilli();
        assertEquals(expected, DateUtils.toEpochMilli(2024, 6, 15, 10, 30, 0, 0, 0));
    }
}
