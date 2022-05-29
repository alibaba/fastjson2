package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue261 {
    @Test
    public void test0() {
        assertNull(JSON.parseObject("\"\"", LocalDateTime.class));
        assertNull(JSON.parseObject("\"null\"", LocalDateTime.class));
        assertNull(JSON.parseObject("null", LocalDateTime.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", LocalDateTime.class)
        );

        assertNull(JSON.parseObject("\"\"", LocalDate.class));
        assertNull(JSON.parseObject("\"null\"", LocalDate.class));
        assertNull(JSON.parseObject("null", LocalDate.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", LocalDate.class)
        );

        assertNull(JSON.parseObject("\"\"", LocalTime.class));
        assertNull(JSON.parseObject("\"null\"", LocalTime.class));
        assertNull(JSON.parseObject("null", LocalTime.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", LocalTime.class)
        );

        assertNull(JSON.parseObject("\"\"", ZonedDateTime.class));
        assertNull(JSON.parseObject("\"null\"", Instant.class));
        assertNull(JSON.parseObject("null", ZonedDateTime.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", ZonedDateTime.class)
        );

        assertNull(JSON.parseObject("\"\"", Instant.class));
        assertNull(JSON.parseObject("\"null\"", Instant.class));
        assertNull(JSON.parseObject("null", Instant.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", Instant.class)
        );

        assertNull(JSON.parseObject("\"\"", Date.class));
        assertNull(JSON.parseObject("\"null\"", Date.class));
        assertNull(JSON.parseObject("null", Date.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", Date.class)
        );

        assertNull(
                JSON.parseObject("\"\"", Calendar.class));
        assertNull(JSON.parseObject("\"null\"", Calendar.class));
        assertNull(JSON.parseObject("null", Calendar.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", Calendar.class)
        );

        assertNull(
                JSON.parseObject("\"\"", java.sql.Date.class));
        assertNull(JSON.parseObject("\"null\"", java.sql.Date.class));
        assertNull(JSON.parseObject("null", java.sql.Date.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", java.sql.Date.class)
        );

        assertNull(
                JSON.parseObject("\"\"", java.sql.Timestamp.class));
        assertNull(JSON.parseObject("\"null\"", java.sql.Timestamp.class));
        assertNull(JSON.parseObject("null", java.sql.Timestamp.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", java.sql.Timestamp.class)
        );

        assertNull(
                JSON.parseObject("\"\"", java.sql.Time.class));
        assertNull(JSON.parseObject("\"null\"", java.sql.Time.class));
        assertNull(JSON.parseObject("null", java.sql.Time.class));

        assertNull(JSONObject
                .of("datetime", "")
                .getObject("datetime", java.sql.Time.class)
        );
    }

    @Test
    public void test1() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean1.class).date);
        assertNull(JSON.parseObject("{\"date\":\"null\"}", Bean1.class).date);
        assertNull(JSON.parseObject("{\"date\":null}", Bean1.class).date);
    }

    public static class Bean1 {
        LocalDateTime date;
    }

    @Test
    public void test2() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean2.class).date);
        assertNull(JSON.parseObject("{\"date\":\"null\"}", Bean2.class).date);
        assertNull(JSON.parseObject("{\"date\":null}", Bean2.class).date);
    }

    public static class Bean2 {
        LocalDate date;
    }

    @Test
    public void test3() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean3.class).date);
        assertNull(JSON.parseObject("{\"date\":\"null\"}", Bean3.class).date);
        assertNull(JSON.parseObject("{\"date\":null}", Bean3.class).date);
    }

    public static class Bean3 {
        LocalTime date;
    }

    @Test
    public void test4() {
        assertNull(JSON.parseObject("{\"date\":\"\"}", Bean4.class).date);
        assertNull(JSON.parseObject("{\"date\":\"null\"}", Bean4.class).date);
        assertNull(JSON.parseObject("{\"date\":null}", Bean4.class).date);
    }

    public static class Bean4 {
        ZonedDateTime date;
    }
}
