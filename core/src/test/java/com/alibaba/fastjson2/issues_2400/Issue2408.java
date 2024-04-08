package com.alibaba.fastjson2.issues_2400;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.DateUtils;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2408 {
    static final long millis = 1712557951977L;
    static final long seconds = millis / 1000;
    static final Instant instant = new Date(millis).toInstant();

    @Test
    public void test() {
        Timestamp timestamp = new Timestamp(millis);
        byte[] bytes = JSONB.toBytes(timestamp, JSONWriter.Feature.WriteClassName);
        Date date = JSONB.parseObject(bytes, Date.class);
        assertEquals(millis, date.getTime());
    }

    @Test
    public void testInstant() {
        byte[] bytes = JSONB.toBytes(instant);
        Date date = JSONB.parseObject(bytes, Date.class);
        assertEquals(millis, date.getTime());
    }

    @Test
    public void testLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, DateUtils.DEFAULT_ZONE_ID);
        byte[] bytes = JSONB.toBytes(ldt);
        Date date = JSONB.parseObject(bytes, Date.class);
        assertEquals(millis, date.getTime());
    }

    @Test
    public void testLocalTime() {
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, DateUtils.DEFAULT_ZONE_ID);
        LocalTime localTime = ldt.toLocalTime();
        byte[] bytes = JSONB.toBytes(localTime);
        Date date = JSONB.parseObject(bytes, Date.class);
        assertEquals(23551977, date.getTime());
    }

    @Test
    public void testLocalDate() {
        LocalDateTime ldt = LocalDateTime.ofInstant(instant, DateUtils.DEFAULT_ZONE_ID);
        LocalDate localDate = ldt.toLocalDate();
        byte[] bytes = JSONB.toBytes(localDate);
        Date date = JSONB.parseObject(bytes, Date.class);
        assertEquals(1712505600000L, date.getTime());
    }

    @Test
    public void testZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, DateUtils.DEFAULT_ZONE_ID);
        byte[] bytes = JSONB.toBytes(zdt);
        Date date = JSONB.parseObject(bytes, Date.class);
        assertEquals(millis, date.getTime());
    }

    @Test
    public void testSeconds() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeMillis(seconds * 1000);
        byte[] bytes = jsonWriter.getBytes();
        Date date = JSONReader.ofJSONB(bytes)
                .readDate();
        assertEquals(seconds * 1000, date.getTime());
    }

    @Test
    public void testMillis() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeMillis(millis);
        byte[] bytes = jsonWriter.getBytes();
        Date result = JSONReader.ofJSONB(bytes)
                .readDate();
        assertEquals(millis, result.getTime());
    }

    @Test
    public void testMinutes() {
        long millis = 2712505800000L;
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeMillis(millis);
        byte[] bytes = jsonWriter.getBytes();
        Date date = JSONReader.ofJSONB(bytes).readDate();
        assertEquals(millis, date.getTime());
    }
}
