package com.alibaba.fastjson2.support.csv;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVReaderReviewRegressionTest {
    @Test
    public void escapedQuotesPreserveLastCharacter() throws Exception {
        String csv = "\"a\"\"b\",\"c\"\"d\"\n";
        for (CSVReader reader : new CSVReader[]{CSVReader.of(csv.getBytes(StandardCharsets.UTF_8)), CSVReader.of(csv.toCharArray())}) {
            try (CSVReader closeable = reader) {
                assertArrayEquals(new String[]{"a\"b", "c\"d"}, reader.readLine());
            }
        }
        List<String> values = new ArrayList<>();
        try (CSVReader reader = CSVReader.of(csv.getBytes(StandardCharsets.UTF_8),
                (row, column, bytes, off, len, charset) -> values.add(new String(bytes, off, len, charset)))) {
            reader.readAll();
        }
        assertEquals(Arrays.asList("a\"b", "c\"d"), values);
        values.clear();
        char[] chars = csv.toCharArray();
        try (CSVReader reader = CSVReader.of(chars, 0, chars.length,
                (row, column, bytes, off, len) -> values.add(new String(bytes, off, len)))) {
            reader.readAll();
        }
        assertEquals(Arrays.asList("a\"b", "c\"d"), values);
    }

    @Test
    public void shortInputStreamReadsPreserveEarlierBytes() throws Exception {
        byte[] input = "name\nfirst\nsecond\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(input) {
            @Override
            public synchronized int read(byte[] bytes, int off, int len) {
                return super.read(bytes, off, Math.min(len, 3));
            }
        };
        try (CSVReader reader = CSVReader.of(stream)) {
            assertArrayEquals(new String[]{"name"}, reader.readLine());
            assertArrayEquals(new String[]{"first"}, reader.readLine());
            assertArrayEquals(new String[]{"second"}, reader.readLine());
            assertNull(reader.readLine());
        }
    }

    @Test
    public void utf16ByteSliceUsesRequestedCharset() throws Exception {
        for (Charset charset : new Charset[]{StandardCharsets.UTF_16, StandardCharsets.UTF_16BE, StandardCharsets.UTF_16LE}) {
            byte[] data = "name\ncaf\u00e9\n".getBytes(charset);
            byte[] input = new byte[data.length + 6];
            System.arraycopy(data, 0, input, 3, data.length);
            try (CSVReader<NameBean> reader = CSVReader.of(input, 3, data.length, charset, NameBean.class)) {
                reader.readHeader();
                assertEquals("caf\u00e9", reader.readLineObject().name);
            }
        }
    }

    @Test
    public void compactLatin1StringIsNotDecodedAsUtf8() throws Exception {
        try (CSVReader reader = CSVReader.of("caf\u00e9\n")) {
            assertArrayEquals(new String[]{"caf\u00e9"}, reader.readLine());
        }
    }

    @Test
    public void charArrayRowCountMatchesOtherInputs() {
        for (String input : new String[]{"a", "abc", "a,b\nc,d", "\r\n", "a\rb\r", "a\n\nb"}) {
            assertEquals(CSVReader.rowCount(input), CSVReader.rowCount(input.toCharArray()));
            assertEquals(CSVReader.rowCount(input, CSVReader.Feature.IgnoreEmptyLine),
                    CSVReader.rowCount(input.toCharArray(), CSVReader.Feature.IgnoreEmptyLine));
        }
    }

    public static class NameBean {
        public String name;
    }
}
