package com.alibaba.fastjson2.issues_4000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4003 {
    @Test
    public void testInstantWithISO8601Format() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Instant instant = Instant.parse("2026-02-24T14:50:00Z");

        String json = JSON.toJSONString(instant, JSONWriter.Feature.UseISO8601DateFormat);

        assertEquals("\"2026-02-24T14:50:00Z\"", json);
    }

    @Test
    public void testInstantWithISO8601FormatWithMillis() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Instant instant = Instant.parse("2026-02-24T14:50:00.123Z");

        String json = JSON.toJSONString(instant, JSONWriter.Feature.UseISO8601DateFormat);
        assertEquals("\"2026-02-24T14:50:00.123Z\"", json);
    }

    @Test
    public void testInstantWithoutISO8601Format() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Instant instant = Instant.parse("2026-02-24T14:50:00Z");

        String json = JSON.toJSONString(instant);

        assertEquals("\"2026-02-24T14:50:00Z\"", json);
    }
}
