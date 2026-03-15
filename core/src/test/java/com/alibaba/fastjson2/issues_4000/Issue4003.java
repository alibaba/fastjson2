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
}
