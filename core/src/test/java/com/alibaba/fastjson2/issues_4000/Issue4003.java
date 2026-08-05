package com.alibaba.fastjson2.issues_4000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Issue 4003: when WriteISO8601DateFormat is requested at runtime but the
 * JSONWriter context already carries a default dateFormat such as
 * "yyyy-MM-dd HH:mm:ss" (for example via Spring Boot's FastJsonConfig),
 * Instant was being serialized as "yyyy-MM-dd HH:mm:ss" — the explicit
 * ISO8601 feature should take priority.
 */
public class Issue4003 {
    public static class Bean {
        public Instant time;
    }

    private static JSONWriter.Context utcContext() {
        JSONWriter.Context ctx = new JSONWriter.Context();
        ctx.setZoneId(ZoneId.of("UTC"));
        return ctx;
    }

    private static JSONWriter.Context utcContext(String dateFormat) {
        JSONWriter.Context ctx = new JSONWriter.Context(dateFormat);
        ctx.setZoneId(ZoneId.of("UTC"));
        return ctx;
    }

    @Test
    public void testIso8601ContextDateFormat() {
        Bean b = new Bean();
        b.time = Instant.parse("2026-02-24T14:50:00Z");
        JSONWriter.Context ctx = utcContext();
        ctx.setDateFormat("iso8601");
        String json = JSON.toJSONString(b, ctx);
        assertEquals("{\"time\":\"2026-02-24T14:50:00Z\"}", json);
    }

    @Test
    public void testIso8601OverridesDefaultDateFormat() {
        Bean b = new Bean();
        b.time = Instant.parse("2026-02-24T14:50:00Z");
        // Simulates Spring Boot's FastJsonConfig: default dateFormat is
        // "yyyy-MM-dd HH:mm:ss", then the user later switches to ISO8601.
        JSONWriter.Context ctx = utcContext("yyyy-MM-dd HH:mm:ss");
        ctx.setDateFormat("iso8601");
        String json = JSON.toJSONString(b, ctx);
        assertEquals("{\"time\":\"2026-02-24T14:50:00Z\"}", json);
    }

    @Test
    public void testDefaultDateFormatStillUsedWithoutIso8601() {
        Bean b = new Bean();
        b.time = Instant.parse("2026-02-24T14:50:00Z");
        JSONWriter.Context ctx = utcContext("yyyy-MM-dd HH:mm:ss");
        String json = JSON.toJSONString(b, ctx);
        assertEquals("{\"time\":\"2026-02-24 14:50:00\"}", json);
    }

    @Test
    public void testNoFeatureNoFormat() {
        Bean b = new Bean();
        b.time = Instant.parse("2026-02-24T14:50:00Z");
        JSONWriter.Context ctx = utcContext();
        String json = JSON.toJSONString(b, ctx);
        assertEquals("{\"time\":\"2026-02-24T14:50:00Z\"}", json);
    }
}
