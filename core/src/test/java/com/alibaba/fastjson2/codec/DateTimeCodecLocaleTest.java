package com.alibaba.fastjson2.codec;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DateTimeCodecLocaleTest {
    @Test
    public void localeOverridesDoNotPolluteConfiguredFormatter() {
        DateTimeCodec codec = new DateTimeCodec("MMMM", Locale.ENGLISH) { };
        LocalDate date = LocalDate.of(2024, 1, 1);
        assertEquals("janvier", codec.getDateFormatter(Locale.FRENCH).format(date));
        assertEquals("January", codec.getDateFormatter().format(date));
        assertEquals("January", codec.getDateFormatter(Locale.ENGLISH).format(date));
        assertEquals("janvier", codec.getDateFormatter(Locale.FRENCH).format(date));
        assertEquals("January", codec.getDateFormatter(null).format(date));
        assertSame(codec.getDateFormatter(), codec.getDateFormatter(Locale.ENGLISH));
    }

    @Test
    public void localeOverridesDoNotPolluteDefaultFormatter() {
        DateTimeCodec codec = new DateTimeCodec("MMMM") { };
        Locale initialLocale = codec.getDateFormatter().getLocale();
        Locale override = Locale.FRENCH.equals(initialLocale) ? Locale.ENGLISH : Locale.FRENCH;
        assertEquals(override, codec.getDateFormatter(override).getLocale());
        assertEquals(initialLocale, codec.getDateFormatter().getLocale());
        assertEquals(initialLocale, codec.getDateFormatter(null).getLocale());
    }

    @Test
    public void specialFormatsDoNotCreateFormatters() {
        for (String format : new String[] {null, "millis", "unixtime", "iso8601"}) {
            DateTimeCodec codec = new DateTimeCodec(format, Locale.ENGLISH) { };
            assertNull(codec.getDateFormatter(Locale.FRENCH));
            assertNull(codec.getDateFormatter(null));
        }
    }
}
