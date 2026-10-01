package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CSVWriterReviewRegressionTest {
    @Test
    public void quoteSpecialCharactersWithoutComma() throws Exception {
        for (CSVWriter writer : writers()) {
            try (CSVWriter closeable = writer) {
                writer.writeLine("a\nb", "a\rb", "a\"b", "plain", "");
                assertEquals("\"a\nb\",\"a\rb\",\"a\"\"b\",plain,\n", writer.toString());
            }
        }
    }

    @Test
    public void quotedFieldSpansBuffers() throws Exception {
        String value = "," + repeat('a', 600000) + "\"end";
        for (CSVWriter writer : writers()) {
            try (CSVWriter closeable = writer) {
                writer.writeLine("first", value, "last");
                assertEquals("first,\"" + value.replace("\"", "\"\"") + "\",last\n", writer.toString());
            }
        }
    }

    @Test
    public void decimalAndNanosecondsAtBufferBoundary() throws Exception {
        String prefix = repeat('a', 512 * 1024 - 25);
        BigDecimal decimal = new BigDecimal(repeat('1', 100) + ".5");
        for (CSVWriter writer : writers()) {
            try (CSVWriter closeable = writer) {
                writer.writeString(prefix);
                writer.writeDecimal(decimal);
                assertEquals(prefix + decimal, writer.toString());
            }
        }
        prefix = repeat('a', 512 * 1024 - 20);
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 2, 3, 4, 5, 123456789);
        for (CSVWriter writer : writers()) {
            try (CSVWriter closeable = writer) {
                writer.writeString(prefix);
                writer.writeLocalDateTime(dateTime);
                assertEquals(prefix + "2024-01-02 03:04:05.123456789", writer.toString());
            }
        }
    }

    @Test
    public void stringEncodingAndNull() throws Exception {
        for (Charset charset : new Charset[]{StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1}) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (CSVWriter writer = CSVWriter.of(output, charset)) {
                writer.writeString((String) null);
                writer.writeLine("caf\u00e9");
                assertEquals("caf\u00e9\n", writer.toString());
            }
            assertEquals("caf\u00e9\n", new String(output.toByteArray(), charset));
        }
    }

    @Test
    public void alwaysQuoteStringsAndInstant() throws Exception {
        CSVWriter[] writers = {
                new CSVWriterUTF8(new ByteArrayOutputStream(), StandardCharsets.UTF_8, ZoneOffset.UTC, CSVWriter.Feature.AlwaysQuoteStrings),
                new CSVWriterUTF16(new StringWriter(), ZoneOffset.UTC, CSVWriter.Feature.AlwaysQuoteStrings)
        };
        for (CSVWriter writer : writers) {
            try (CSVWriter closeable = writer) {
                writer.writeLine("plain", "", Instant.parse("2024-01-02T03:04:05.123456789Z"));
                assertEquals("\"plain\",\"\",2024-01-02 03:04:05.123456789\n", writer.toString());
            }
        }
    }

    private static CSVWriter[] writers() {
        return new CSVWriter[]{CSVWriter.of(), CSVWriter.of(new StringWriter())};
    }

    private static String repeat(char value, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, value);
        return new String(chars);
    }
}
