package com.alibaba.fastjson2.issues_4000;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue4003 {
    private static String toJSONString(Instant instant, String format) {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.setZoneId(ZoneId.of("UTC"));
        if (format != null) {
            context.setDateFormat(format);
        }
        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.writeAny(instant);
            return writer.toString();
        }
    }

    @Test
    public void testInstantWithISO8601Format() {
        Instant instant = Instant.parse("2026-02-24T14:50:00Z");
        String json = toJSONString(instant, "iso8601");

        assertEquals("\"2026-02-24T14:50:00Z\"", json);
    }

    @Test
    public void testInstantWithISO8601FormatWithMillis() {
        Instant instant = Instant.parse("2026-02-24T14:50:00.123Z");
        String json = toJSONString(instant, "iso8601");
        assertEquals("\"2026-02-24T14:50:00.123Z\"", json);
    }

    @Test
    public void testInstantWithoutISO8601Format() {
        Instant instant = Instant.parse("2026-02-24T14:50:00Z");
        String json = toJSONString(instant, null);

        assertEquals("\"2026-02-24T14:50:00Z\"", json);
    }

    @Test
    public void testInstantWithNonUTCZone() {
        Instant instant = Instant.parse("2026-02-24T14:50:00Z");
        JSONWriter.Context context = JSONFactory.createWriteContext();
        context.setZoneId(ZoneId.of("Asia/Shanghai"));
        try (JSONWriter writer = JSONWriter.of(context)) {
            writer.writeAny(instant);
            assertEquals("\"2026-02-24T14:50:00Z\"", writer.toString());
        }
    }

    @Test
    public void testInstantWithUnixTimeFormat() {
        Instant instant = Instant.parse("2026-02-24T14:50:00Z");
        String json = toJSONString(instant, "unixtime");
        assertEquals("1771944600", json);
    }

    @Test
    public void testInstantWithMillisFormat() {
        Instant instant = Instant.parse("2026-02-24T14:50:00.123Z");
        String json = toJSONString(instant, "millis");
        assertEquals("1771944600123", json);
    }
}
