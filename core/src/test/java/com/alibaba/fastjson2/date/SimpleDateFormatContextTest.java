package com.alibaba.fastjson2.date;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleDateFormatContextTest {
    @Test
    public void textReaderUsesContextLocaleAndZone() {
        assertConfigured(JSONReader.of("\"MMM\"", context()));
    }

    @Test
    public void utf8ReaderUsesContextLocaleAndZone() {
        byte[] json = "\"MMM\"".getBytes(StandardCharsets.UTF_8);
        assertConfigured(JSONReader.of(json, context()));
    }

    @Test
    public void jsonbReaderUsesContextLocaleAndZone() {
        assertConfigured(JSONReader.ofJSONB(JSONB.toBytes("MMM"), context()));
    }

    private static void assertConfigured(JSONReader reader) {
        try (JSONReader ignored = reader) {
            SimpleDateFormat format = reader.read(SimpleDateFormat.class);
            assertEquals("GMT+08:00", format.getTimeZone().getID());
            assertEquals("janv.", format.format(new Date(0)));
        }
    }

    private static JSONReader.Context context() {
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setLocale(Locale.FRANCE);
        context.setZoneId(ZoneId.of("GMT+08:00"));
        return context;
    }
}
