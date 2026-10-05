package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class BaseReaderReviewTest {
    @Test
    void magnitudeConversionsDoNotLoseHighWords() {
        String large = BigInteger.ONE.shiftLeft(96).toString();
        try (JSONReader reader = JSONReader.of(large)) {
            reader.readNumber0();
            assertThrows(JSONException.class, reader::getInt32Value);
            assertThrows(JSONException.class, reader::getInt64Value);
            assertEquals(new BigDecimal(large), reader.getBigDecimal());
            assertEquals(new BigInteger(large).doubleValue(), reader.getDoubleValue());
        }
        try (JSONReader reader = JSONReader.of("4294967295")) {
            reader.readNumber0();
            assertEquals(4294967295D, reader.getDoubleValue());
        }
    }

    @Test
    void decimalConversionsRetainPrecision() {
        try (JSONReader reader = JSONReader.of("12345678901234567890.25")) {
            assertEquals(new BigInteger("12345678901234567890"), reader.readBigInteger());
        }
        try (JSONReader reader = JSONReader.of("1.234567890123456789e20")) {
            reader.readNumber0();
            assertEquals(0, new BigDecimal("1.234567890123456789e20").compareTo(reader.getBigDecimal()));
        }
    }

    @Test
    void wrappedLongValuesDoNotNarrowToInt() {
        for (String text : new String[] {"{\"val\":4294967296}", "[4294967296]"}) {
            try (JSONReader reader = JSONReader.of(text)) {
                reader.readNumber0();
                assertEquals(4294967296L, reader.getInt64Value());
            }
        }
    }

    @Test
    void nullableConvenienceReadersReturnNull() {
        try (JSONReader reader = JSONReader.of("null")) {
            assertNull(reader.readBase64());
        }
        try (JSONReader reader = JSONReader.of("null")) {
            assertNull(reader.readCharacter());
        }
    }

    @Test
    void dateContextCanChangePatternAndLocale() {
        JSONReader.Context context = new JSONReader.Context("yyyy-MM-dd HH:mm:ss");
        context.setDateFormat("dd/MM/yyyy");
        assertFalse(context.isFormatyyyyMMddhhmmss19());
        try (JSONReader reader = JSONReader.of("\"31/12/2024\"", context)) {
            assertEquals(LocalDate.of(2024, 12, 31), reader.readLocalDate());
        }
        context.setDateFormat("yyyy-MM-dd HH:mm");
        assertTrue(context.isFormatHasHour());
        context.setDateFormat("MMM");
        context.setLocale(Locale.ENGLISH);
        assertEquals("Jan", context.getDateFormatter().format(LocalDate.of(2024, 1, 1)));
        context.setLocale(Locale.FRENCH);
        assertEquals("janv.", context.getDateFormatter().format(LocalDate.of(2024, 1, 1)));
    }

    @Test
    void numericDatesHonorUnixTimeAndDotNetDatesKeepNegativeEpoch() {
        JSONReader.Context context = new JSONReader.Context("unixtime");
        context.setZoneId(ZoneOffset.UTC);
        try (JSONReader reader = JSONReader.of("1", context)) {
            assertEquals(LocalDateTime.of(1970, 1, 1, 0, 0, 1), reader.readLocalDateTime());
        }
        try (JSONReader reader = JSONReader.of("1", context)) {
            assertEquals(1000, reader.readDate().getTime());
        }
        for (String value : new String[] {"/Date(-1000)/", "/Date(-1000-0500)/"}) {
            try (JSONReader reader = JSONReader.of(JSON.toJSONString(value))) {
                assertEquals(-1000, reader.readMillisFromString());
            }
        }
    }
}
