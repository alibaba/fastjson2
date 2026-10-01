package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class TextWriterReviewTest {
    @Test
    void scalarAndArrayWritesGrowForQuotes() {
        assertSmallBuffer("\"false\"", w -> w.writeString(false), 0);
        assertSmallBuffer("\"-128\"", w -> w.writeInt8(Byte.MIN_VALUE), 5,
                JSONWriter.Feature.WriteNonStringValueAsString);
        assertSmallBuffer("\"-32768\"", w -> w.writeInt16(Short.MIN_VALUE), 7,
                JSONWriter.Feature.WriteNonStringValueAsString);
        byte[] values = new byte[20];
        Arrays.fill(values, Byte.MIN_VALUE);
        assertSmallBuffer(JSON.toJSONString(values, JSONWriter.Feature.WriteNonStringValueAsString),
                w -> w.writeInt8(values), 102, JSONWriter.Feature.WriteNonStringValueAsString);
        assertSmallBuffer("[]", w -> w.writeDouble(new double[0]), 1);
    }

    @Test
    void slicesEscapeOnlyRequestedCharacters() {
        assertSmallBuffer("\"\\u0000\\u0000\"", w -> w.writeString(new char[2], 0, 2, true), 6);
        assertSmallBuffer("\"a\\\"\"", w -> w.writeString("xxa\"".toCharArray(), 2, 2), 6);
        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            writer.writeString("x\uD83D\uDE00".toCharArray(), 1, 1);
            assertEquals("\"?\"", writer.toString());
        }
    }

    @Test
    void rawStringsUseStandardUtf8() {
        String raw = "a\u0000\uD83D\uDE00";
        try (JSONWriter writer = JSONWriter.ofUTF8()) {
            writer.writeRaw(raw);
            assertArrayEquals(raw.getBytes(StandardCharsets.UTF_8), writer.getBytes());
            assertThrows(JSONException.class, () -> writer.writeRaw('\u0080'));
            assertThrows(JSONException.class, () -> writer.writeRaw('a', '\u0080'));
        }
    }

    @Test
    void flushTranscodesAndClearsBuffer() throws Exception {
        for (Charset charset : new Charset[] {StandardCharsets.ISO_8859_1, StandardCharsets.US_ASCII}) {
            try (JSONWriter writer = JSONWriter.ofUTF8()) {
                writer.writeString("caf\u00e9");
                byte[] expected = "\"caf\u00e9\"".getBytes(charset);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                assertEquals(expected.length, writer.flushTo(out, charset));
                assertArrayEquals(expected, out.toByteArray());
                assertEquals(0, writer.flushTo(out, charset));
            }
        }
    }

    @Test
    void negativeSubHourOffsetAndSingleQuotedUuid() {
        assertSmallBuffer("\"2024-01-02T03:04:05-00:30\"",
                w -> w.writeDateTimeISO8601(2024, 1, 2, 3, 4, 5, 0, -1800, true), 32);
        UUID uuid = UUID.fromString("12345678-1234-5678-9abc-123456789abc");
        assertSmallBuffer("'" + uuid + "'", w -> w.writeUUID(uuid), 38, JSONWriter.Feature.UseSingleQuotes);
    }

    @Test
    void extremeYearsReserveEnoughSpace() {
        assertSmallBuffer("\"-999999999-01-02\"", w -> w.writeDateYYYMMDD10(-999999999, 1, 2), 13);
        assertSmallBuffer("\"-999999999-01-02T03:04:05.123-00:30\"",
                w -> w.writeDateTimeISO8601(-999999999, 1, 2, 3, 4, 5, 123, -1800, true), 31);
        OffsetDateTime dateTime = OffsetDateTime.of(LocalDateTime.MIN.withNano(123456789), ZoneOffset.ofHoursMinutesSeconds(1, 2, 3));
        assertSmallBuffer("\"-999999999-01-01T00:00:00.123456789+01:02:03\"", w -> w.writeOffsetDateTime(dateTime), 45);
    }

    @Test
    void prettySlicedNamesMatchWholeNames() {
        try (JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat))) {
            writer.startObject();
            writer.writeNameRaw("\"a\":".toCharArray(), 0, 4);
            writer.writeInt32(1);
            writer.writeNameRaw("\"b\":".toCharArray(), 0, 4);
            writer.writeInt32(2);
            writer.endObject();
            assertEquals("{\n\t\"a\":1,\n\t\"b\":2\n}", writer.toString());
        }
    }

    private static void assertSmallBuffer(String expected, Consumer<JSONWriter> action, int capacity,
                                          JSONWriter.Feature... features) {
        try (JSONWriterUTF8 utf8 = new JSONWriterUTF8(JSONFactory.createWriteContext(features));
                JSONWriterUTF16 utf16 = new JSONWriterUTF16(JSONFactory.createWriteContext(features))) {
            utf8.bytes = new byte[capacity];
            utf16.chars = new char[capacity];
            action.accept(utf8);
            action.accept(utf16);
            assertEquals(expected, utf8.toString());
            assertEquals(expected, utf16.toString());
        }
    }
}
