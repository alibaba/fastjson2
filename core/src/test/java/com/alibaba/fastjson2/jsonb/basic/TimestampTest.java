package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimestampTest {
    /**
     * 0xae int32_epochSecond int32_nanoAdjustment
     */
    @Test
    public void testInstant() {
        long epochSecond = 1673103460L;
        int nanoAdjustment = 595000000;
        Instant instant = Instant.ofEpochSecond(epochSecond, nanoAdjustment);
        byte[] bytes = JSONB.toBytes(instant);
        assertEquals(11, bytes.length);
        assertEquals(BC_TIMESTAMP, bytes[0]);

        byte[] secondBytes = JSONB.toBytes(epochSecond);
        byte[] nanoBytes = JSONB.toBytes(nanoAdjustment);

        assertEquals(bytes.length, 1 + secondBytes.length + nanoBytes.length);

        for (int i = 0; i < secondBytes.length; i++) {
            assertEquals(bytes[i + 1], secondBytes[i]);
        }

        for (int i = 0; i < nanoBytes.length; i++) {
            assertEquals(bytes[i + 1 + secondBytes.length], nanoBytes[i]);
        }

        Instant parsed = (Instant) JSONB.parse(bytes);
        assertEquals(instant, parsed);
    }

    /**
     * 0xad bytes4_epoch_seconds
     */
    @Test
    public void testTimestampSeconds() {
        long millis = 1673104160000L;
        Date date = new Date(millis);

        byte[] bytes = JSONB.toBytes(date);
        assertEquals(5, bytes.length);
        assertEquals(BC_TIMESTAMP_SECONDS, bytes[0]);

        Date parsed = (Date) JSONB.parse(bytes);
        assertEquals(millis, parsed.getTime());
    }

    /**
     * 0xad bytes4_epoch_minutes
     */
    @Test
    public void testTimestampMinutes() {
        LocalDateTime ldt = LocalDateTime.of(2100, 1, 1, 12, 13, 0);
        long millis = ldt.atZone(DateUtils.SHANGHAI_ZONE_ID).toInstant().toEpochMilli();
        Date date = new Date(millis);

        byte[] bytes = JSONB.toBytes(date);
        assertEquals(5, bytes.length);
        assertEquals(BC_TIMESTAMP_MINUTES, bytes[0]);

        Date parsed = (Date) JSONB.parse(bytes);
        assertEquals(millis, parsed.getTime());
    }

    /**
     * 0xab bytes8_epoch_millis
     */
    @Test
    public void testTimestampMillis() {
        long millis = 1673104161234L;
        Date date = new Date(millis);

        byte[] bytes = JSONB.toBytes(date);
        assertEquals(9, bytes.length);
        assertEquals(BC_TIMESTAMP_MILLIS, bytes[0]);

        Date parsed = (Date) JSONB.parse(bytes);
        assertEquals(millis, parsed.getTime());
    }

    /**
     * 0xaa
     */
    @Test
    public void testTimestampWithTZ() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 7, 15, 22, 53, 89000000);
        ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);

        byte[] bytes = JSONB.toBytes(zdt);
        assertEquals(15, bytes.length);
        assertEquals(BC_TIMESTAMP_WITH_TIMEZONE, bytes[0]);
        byte b1 = bytes[1];
        byte b2 = bytes[2];
        byte b3 = bytes[3];
        byte b4 = bytes[4];
        byte b5 = bytes[5];
        byte b6 = bytes[6];
        byte b7 = bytes[7];

        int year = (b1 << 8) + (b2 & 0xFF);
        int month = b3;
        int dayOfMonth = b4;
        int hour = b5;
        int minute = b6;
        int second = b7;

        assertEquals(ldt.getYear(), year);
        assertEquals(ldt.getMonthValue(), month);
        assertEquals(ldt.getDayOfMonth(), dayOfMonth);
        assertEquals(ldt.getHour(), hour);
        assertEquals(ldt.getMinute(), minute);
        assertEquals(ldt.getSecond(), second);

        byte[] nanoBytes = JSONB.toBytes(ldt.getNano());
        for (int i = 0; i < nanoBytes.length; i++) {
            assertEquals(bytes[i + 8], nanoBytes[i]);
        }

        String zoneId = zdt.getZone().getId();
        byte[] zoneIdBytes = JSONB.toBytes(zoneId);
        for (int i = 0; i < zoneIdBytes.length; i++) {
            assertEquals(bytes[i + 8 + nanoBytes.length], zoneIdBytes[i]);
        }

        assertEquals(bytes.length, 8 + nanoBytes.length + zoneIdBytes.length);

        ZonedDateTime parsed = (ZonedDateTime) JSONB.parse(bytes);
        assertEquals(zdt, parsed);
    }

    /**
     * 0xa9 byte_y1 byte_b2 byte_month byte_dayOfMonth
     */
    @Test
    public void testLocalDate() {
        LocalDate localDate = LocalDate.of(2023, 1, 7);
        byte[] bytes = JSONB.toBytes(localDate);
        assertEquals(5, bytes.length);
        assertEquals(BC_LOCAL_DATE, bytes[0]);

        byte b1 = bytes[1];
        byte b2 = bytes[2];
        byte b3 = bytes[3];
        byte b4 = bytes[4];

        int year = (b1 << 8) + (b2 & 0xFF);
        int month = b3;
        int dayOfMonth = b4;

        assertEquals(localDate.getYear(), year);
        assertEquals(localDate.getMonthValue(), month);
        assertEquals(localDate.getDayOfMonth(), dayOfMonth);

        LocalDate parsed = (LocalDate) JSONB.parse(bytes);
        assertEquals(localDate, parsed);
    }

    /**
     * 0xa8
     */
    @Test
    public void testLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 7, 15, 22, 53, 89000000);
        byte[] bytes = JSONB.toBytes(ldt);
        assertEquals(13, bytes.length);
        assertEquals(BC_LOCAL_DATETIME, bytes[0]);

        byte b1 = bytes[1];
        byte b2 = bytes[2];
        byte b3 = bytes[3];
        byte b4 = bytes[4];
        byte b5 = bytes[5];
        byte b6 = bytes[6];
        byte b7 = bytes[7];

        int year = (b1 << 8) + (b2 & 0xFF);
        int month = b3;
        int dayOfMonth = b4;
        int hour = b5;
        int minute = b6;
        int second = b7;

        assertEquals(ldt.getYear(), year);
        assertEquals(ldt.getMonthValue(), month);
        assertEquals(ldt.getDayOfMonth(), dayOfMonth);
        assertEquals(ldt.getHour(), hour);
        assertEquals(ldt.getMinute(), minute);
        assertEquals(ldt.getSecond(), second);

        byte[] nanoBytes = JSONB.toBytes(ldt.getNano());
        for (int i = 0; i < nanoBytes.length; i++) {
            assertEquals(bytes[i + 8], nanoBytes[i]);
        }

        assertEquals(bytes.length, 8 + nanoBytes.length);

        LocalDateTime parsed = (LocalDateTime) JSONB.parse(bytes);
        assertEquals(ldt, parsed);
    }

    /**
     * 0xa8
     */
    @Test
    public void testLocalDateTime1() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 7, 15, 22, 53, 0);
        byte[] bytes = JSONB.toBytes(ldt);
        assertEquals(9, bytes.length);
        assertEquals(BC_LOCAL_DATETIME, bytes[0]);

        byte b1 = bytes[1];
        byte b2 = bytes[2];
        byte b3 = bytes[3];
        byte b4 = bytes[4];
        byte b5 = bytes[5];
        byte b6 = bytes[6];
        byte b7 = bytes[7];

        int year = (b1 << 8) + (b2 & 0xFF);
        int month = b3;
        int dayOfMonth = b4;
        int hour = b5;
        int minute = b6;
        int second = b7;

        assertEquals(ldt.getYear(), year);
        assertEquals(ldt.getMonthValue(), month);
        assertEquals(ldt.getDayOfMonth(), dayOfMonth);
        assertEquals(ldt.getHour(), hour);
        assertEquals(ldt.getMinute(), minute);
        assertEquals(ldt.getSecond(), second);

        byte[] nanoBytes = JSONB.toBytes(ldt.getNano());
        for (int i = 0; i < nanoBytes.length; i++) {
            assertEquals(bytes[i + 8], nanoBytes[i]);
        }

        assertEquals(bytes.length, 8 + nanoBytes.length);

        LocalDateTime parsed = (LocalDateTime) JSONB.parse(bytes);
        assertEquals(ldt, parsed);
    }

    /**
     * 0xa7
     */
    @Test
    public void testLocalTime() {
        LocalTime localTime = LocalTime.of(15, 22, 53, 89000000);
        byte[] bytes = JSONB.toBytes(localTime);
        assertEquals(9, bytes.length);
        assertEquals(BC_LOCAL_TIME, bytes[0]);

        byte b1 = bytes[1];
        byte b2 = bytes[2];
        byte b3 = bytes[3];

        int hour = b1;
        int minute = b2;
        int second = b3;

        assertEquals(localTime.getHour(), hour);
        assertEquals(localTime.getMinute(), minute);
        assertEquals(localTime.getSecond(), second);

        byte[] nanoBytes = JSONB.toBytes(localTime.getNano());
        for (int i = 0; i < nanoBytes.length; i++) {
            assertEquals(bytes[i + 4], nanoBytes[i]);
        }

        assertEquals(bytes.length, 4 + nanoBytes.length);

        LocalTime parsed = (LocalTime) JSONB.parse(bytes);
        assertEquals(localTime, parsed);
    }

    /**
     * 0xa7
     */
    @Test
    public void testLocalTime1() {
        LocalTime localTime = LocalTime.of(15, 22, 53, 0);
        byte[] bytes = JSONB.toBytes(localTime);
        assertEquals(5, bytes.length);
        assertEquals(BC_LOCAL_TIME, bytes[0]);

        byte b1 = bytes[1];
        byte b2 = bytes[2];
        byte b3 = bytes[3];

        int hour = b1;
        int minute = b2;
        int second = b3;

        assertEquals(localTime.getHour(), hour);
        assertEquals(localTime.getMinute(), minute);
        assertEquals(localTime.getSecond(), second);

        byte[] nanoBytes = JSONB.toBytes(localTime.getNano());
        for (int i = 0; i < nanoBytes.length; i++) {
            assertEquals(bytes[i + 4], nanoBytes[i]);
        }

        assertEquals(bytes.length, 4 + nanoBytes.length);

        LocalTime parsed = (LocalTime) JSONB.parse(bytes);
        assertEquals(localTime, parsed);
    }
}
