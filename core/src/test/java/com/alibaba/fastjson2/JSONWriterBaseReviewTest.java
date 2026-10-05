package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class JSONWriterBaseReviewTest {
    @Test
    void boxedFloatKeepsFloatPrecision() {
        for (JSONWriter writer : new JSONWriter[] {JSONWriter.ofUTF8(), JSONWriter.ofUTF16(), JSONWriter.ofJSONB()}) {
            try (JSONWriter output = writer) {
                output.writeFloat(Float.valueOf(0.1F));
                if (output.jsonb) {
                    assertEquals(Float.valueOf(0.1F), JSONB.parse(output.getBytes()));
                } else {
                    assertEquals("0.1", output.toString());
                }
            }
        }
    }

    @Test
    void clearingDateFormatResetsFlags() {
        for (String clear : new String[] {null, ""}) {
            for (String format : new String[] {"millis", "unixtime", "iso8601", "yyyy-MM-dd HH:mm:ss"}) {
                JSONWriter.Context context = new JSONWriter.Context(format);
                context.setDateFormat(clear);
                assertNull(context.getDateFormat());
                assertNull(context.getDateFormatter());
                assertFalse(context.isDateFormatMillis());
                assertFalse(context.isDateFormatUnixTime());
                assertFalse(context.isDateFormatISO8601());
                assertFalse(context.isDateFormatHasDay());
                assertFalse(context.isDateFormatHasHour());
                assertFalse(context.isFormatyyyyMMddhhmmss19());
                try (JSONWriter output = JSONWriter.of(context)) {
                    output.writeLocalDate(LocalDate.of(2024, 1, 2));
                    assertEquals("\"2024-01-02\"", output.toString());
                }
            }
        }
    }

    @Test
    void readerPreservesSurrogateAcrossBufferBoundary() {
        char[] prefix = new char[2047];
        java.util.Arrays.fill(prefix, 'a');
        String value = new String(prefix) + "\uD83D\uDE00";
        try (JSONWriter output = JSONWriter.ofUTF8()) {
            output.writeString(new StringReader(value));
            assertEquals(value, JSON.parseObject(output.getBytes(), String.class));
        }
    }

    @Test
    void pathPreservesNulAndHandlesMalformedSurrogateAtBufferLimit() {
        assertEquals("$.a\u0000b", new JSONWriter.Path(JSONWriter.Path.ROOT, "a\u0000b").toString());
        String prefix = "abcdefghijklmn";
        assertEquals("$." + prefix + "?", new JSONWriter.Path(JSONWriter.Path.ROOT, prefix + "\uDC00").toString());
        assertEquals("$." + prefix + "?z", new JSONWriter.Path(JSONWriter.Path.ROOT, prefix + "\uD800z").toString());
    }
}
