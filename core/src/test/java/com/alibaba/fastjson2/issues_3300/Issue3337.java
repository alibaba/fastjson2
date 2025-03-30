package com.alibaba.fastjson2.issues_3300;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

public class Issue3337 {
    @Test
    public void localDate() {
        LocalDate localDate = LocalDate.now();
        String str = localDate.toString();

        JSONObject object = new JSONObject();
        assertNull(object.getLocalDate("date"));

        object.put("date", str);
        assertEquals(localDate, object.getLocalDate("date"));

        object.put("date", localDate);
        assertEquals(localDate, object.getLocalDate("date"));
    }

    @Test
    public void localDateArray() {
        LocalDate dateTime = LocalDate.now();
        String str = dateTime.toString();

        JSONArray object = new JSONArray();
        object.add(null);
        assertNull(object.getLocalDate(0));

        object.set(0, str);
        assertEquals(dateTime, object.getLocalDate(0));

        object.set(0, dateTime);
        assertEquals(dateTime, object.getLocalDate(0));
        assertSame(dateTime, object.getLocalDate(0));
    }

    @Test
    public void localTime() {
        LocalTime localTime = LocalTime.now();
        String str = localTime.toString();

        JSONObject object = new JSONObject();
        assertNull(object.getLocalTime("time"));

        object.put("time", str);
        assertEquals(localTime, object.getLocalTime("time"));

        object.put("time", localTime);
        assertEquals(localTime, object.getLocalTime("time"));
        assertSame(localTime, object.getLocalTime("time"));
    }

    @Test
    public void localTimeArray() {
        LocalTime dateTime = LocalTime.now();
        String str = dateTime.toString();

        JSONArray object = new JSONArray();
        object.add(null);
        assertNull(object.getLocalTime(0));

        object.set(0, str);
        assertEquals(dateTime, object.getLocalTime(0));

        object.set(0, dateTime);
        assertEquals(dateTime, object.getLocalTime(0));
        assertSame(dateTime, object.getLocalTime(0));
    }

    @Test
    public void offsetTime() {
        OffsetTime localTime = OffsetTime.now();
        String str = localTime.toString();

        JSONObject object = new JSONObject();
        assertNull(object.getOffsetTime("time"));

        object.put("time", str);
        assertEquals(localTime, object.getOffsetTime("time"));

        object.put("time", localTime);
        assertEquals(localTime, object.getOffsetTime("time"));
        assertSame(localTime, object.getOffsetTime("time"));
    }

    @Test
    public void offsetTimeArray() {
        OffsetTime dateTime = OffsetTime.now();
        String str = dateTime.toString();

        JSONArray object = new JSONArray();
        object.add(null);
        assertNull(object.getOffsetTime(0));

        object.set(0, str);
        assertEquals(dateTime, object.getOffsetTime(0));

        object.set(0, dateTime);
        assertEquals(dateTime, object.getOffsetTime(0));
        assertSame(dateTime, object.getOffsetTime(0));
    }

    @Test
    public void localDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        String str = dateTime.toString();

        JSONObject object = new JSONObject();
        assertNull(object.getLocalDateTime("date"));

        object.put("date", str);
        assertEquals(dateTime, object.getLocalDateTime("date"));

        object.put("date", dateTime);
        assertEquals(dateTime, object.getLocalDateTime("date"));
        assertSame(dateTime, object.getLocalDateTime("date"));
    }

    @Test
    public void localDateTimeArray() {
        LocalDateTime dateTime = LocalDateTime.now();
        String str = dateTime.toString();

        JSONArray object = new JSONArray();
        object.add(null);
        assertNull(object.getLocalDateTime(0));

        object.set(0, str);
        assertEquals(dateTime, object.getLocalDateTime(0));

        object.set(0, dateTime);
        assertEquals(dateTime, object.getLocalDateTime(0));
        assertSame(dateTime, object.getLocalDateTime(0));
    }

    @Test
    public void offsetDateTime() {
        OffsetDateTime dateTime = OffsetDateTime.now();
        String str = dateTime.toString();

        JSONObject object = new JSONObject();
        assertNull(object.getOffsetDateTime("date"));

        object.put("date", str);
        assertEquals(dateTime, object.getOffsetDateTime("date"));

        object.put("date", dateTime);
        assertEquals(dateTime, object.getOffsetDateTime("date"));
        assertSame(dateTime, object.getOffsetDateTime("date"));
    }

    @Test
    public void offsetDateTimeArray() {
        OffsetDateTime dateTime = OffsetDateTime.now();
        String str = dateTime.toString();

        JSONArray object = new JSONArray();
        object.add(null);
        assertNull(object.getOffsetDateTime(0));

        object.set(0, str);
        assertEquals(dateTime, object.getOffsetDateTime(0));

        object.set(0, dateTime);
        assertEquals(dateTime, object.getOffsetDateTime(0));
        assertSame(dateTime, object.getOffsetDateTime(0));
    }

    @Test
    public void zonedDateTime() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        String str = dateTime.toString();

        JSONObject object = new JSONObject();
        assertNull(object.getZonedDateTime("date"));

        object.put("date", str);
        assertEquals(dateTime, object.getZonedDateTime("date"));

        object.put("date", dateTime);
        assertEquals(dateTime, object.getZonedDateTime("date"));
        assertSame(dateTime, object.getZonedDateTime("date"));
    }

    @Test
    public void zonedDateTimeArray() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        String str = dateTime.toString();

        JSONArray object = new JSONArray();
        object.add(null);
        assertNull(object.getZonedDateTime(0));

        object.set(0, str);
        assertEquals(dateTime, object.getZonedDateTime(0));

        object.set(0, dateTime);
        assertEquals(dateTime, object.getZonedDateTime(0));
        assertSame(dateTime, object.getZonedDateTime(0));
    }
}
