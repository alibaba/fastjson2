package com.alibaba.fastjson2;

import com.alibaba.fastjson2.reader.ValueConsumer;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

public class TextReaderReviewTest {
    @Test
    public void escapedLatin1ValueHash() {
        for (String text : new String[]{"'abcdefgh\\né'", "\"abcdefgh\\né\""}) {
            for (JSONReader reader : readers(text)) {
                assertEquals(Fnv.hashCode64("abcdefgh\né"), reader.readValueHashCode());
                assertTrue(reader.isEnd());
                assertEquals("abcdefgh\né", reader.getString());
                reader.close();
            }
        }
    }

    @Test
    public void escapedSingleQuotedFieldWithDoubleQuote() {
        for (String text : new String[]{"'a\"b\\nc':1", "'a\"b\\u4E2Dc':1"}) {
            for (JSONReader reader : readers(text)) {
                assertEquals(text.contains("u4E2D") ? "a\"b中c" : "a\"b\nc", reader.readFieldName());
                assertEquals(1, reader.readInt32Value());
                reader.close();
            }
        }
    }

    @Test
    public void internalStringAtEnd() {
        for (String text : new String[]{"'a\\nb'", "\"a\\nb\"", "\"abc\""}) {
            for (JSONReader reader : readers(text)) {
                if (reader instanceof JSONReaderUTF8) {
                    ((JSONReaderUTF8) reader).readString0();
                } else {
                    ((JSONReaderUTF16) reader).readString0();
                }
                assertEquals(text.contains("\\n") ? "a\nb" : "abc", reader.stringValue);
                assertTrue(reader.isEnd());
                reader.close();
            }
        }
    }

    @Test
    public void utf8SingleQuotedDoubleQuote() {
        try (JSONReader reader = utf8("'a\"中b'")) {
            assertEquals("a\"中b", reader.readString());
            assertTrue(reader.isEnd());
        }
    }

    @Test
    public void consumerStringAtEnd() {
        for (String text : new String[]{"'a\\nb'", "\"abc\"", "\"abc\"   "}) {
            try (JSONReader reader = utf8(text)) {
                String[] result = new String[1];
                reader.readString(new ValueConsumer() {
                    @Override
                    public void accept(String value) {
                        result[0] = value;
                    }
                }, false);
                assertEquals(text.contains("\\n") ? "a\nb" : "abc", result[0]);
                assertTrue(reader.isEnd());
            }
        }
    }

    @Test
    public void utf16WhitespaceAndSlices() {
        char[] chars = "prefix 123".toCharArray();
        try (JSONReader reader = new JSONReaderUTF16(JSONFactory.createReadContext(), null, chars, 6, 4)) {
            assertEquals(123, reader.readInt32Value());
        }
        byte[] bytes = " 123".getBytes(StandardCharsets.UTF_16BE);
        try (JSONReader reader = new JSONReaderUTF16(JSONFactory.createReadContext(), new ByteArrayInputStream(bytes))) {
            assertEquals(123, reader.readInt32Value());
        }
        bytes = "   ".getBytes(StandardCharsets.UTF_16BE);
        try (JSONReader reader = new JSONReaderUTF16(JSONFactory.createReadContext(), bytes, 0, bytes.length)) {
            assertTrue(reader.isEnd());
        }
        bytes = "prefix 123".getBytes(StandardCharsets.UTF_16BE);
        try (JSONReader reader = new JSONReaderUTF16(JSONFactory.createReadContext(), bytes, 12, 8)) {
            assertTrue(reader.info().contains("123"));
            assertEquals(123, reader.readInt32Value());
        }
        assertThrows(JSONException.class, () -> new JSONReaderUTF16(JSONFactory.createReadContext(), new byte[1], 0, 1));
    }

    @Test
    public void base64DoesNotInspectFollowingString() {
        for (JSONReader reader : readers("\"YQ==\"  , \"\\n\"")) {
            assertArrayEquals(new byte[]{'a'}, reader.readBase64());
            assertEquals("\n", reader.readString());
            reader.close();
        }
        try (JSONReader reader = utf8("\"abcdefghijkl;\",\"data:image/png;base64,YQ==\"")) {
            reader.readString();
            assertArrayEquals(new byte[]{'a'}, reader.readBase64());
        }
        for (JSONReader reader : readers("null")) {
            assertNull(reader.readBase64());
            assertTrue(reader.isEnd());
            reader.close();
        }
    }

    @Test
    public void booleanAfterNull() {
        for (JSONReader reader : readers("null,true,false")) {
            assertNull(reader.readBool());
            assertEquals(Boolean.TRUE, reader.readBool());
            assertEquals(Boolean.FALSE, reader.readBool());
            reader.close();
        }
    }

    @Test
    public void elevenCharacterDateCursor() {
        for (JSONReader reader : readers("\"01 Jan 2024\",2")) {
            assertEquals(LocalDate.of(2024, 1, 1), reader.readLocalDate11());
            assertEquals(2, reader.readInt32Value());
            reader.close();
        }
    }

    @Test
    public void unterminatedUtf8String() {
        for (String text : new String[]{"\"", "\"a", "\"12345678", "\"1234567890123456", "\"中"}) {
            try (JSONReader reader = utf8(text)) {
                assertThrows(JSONException.class, reader::readString);
            }
        }
    }

    @Test
    public void dotnetDatesBypassFixedWidthParsers() {
        for (String value : new String[]{"/Date(-1)/", "/Date(-1000)/", "/Date(-1000-0500)/", "/Date(-1000+0500)/"}) {
            long expected = value.equals("/Date(-1)/") ? -1 : -1000;
            for (JSONReader reader : readers(JSON.toJSONString(value))) {
                assertEquals(expected, reader.readMillisFromString());
                assertTrue(reader.isEnd());
                reader.close();
            }
            for (JSONReader reader : readers(JSON.toJSONString(value))) {
                reader.getContext().setZoneId(ZoneOffset.UTC);
                assertEquals(LocalDateTime.ofEpochSecond(-1, expected == -1 ? 999000000 : 0, ZoneOffset.UTC), reader.readLocalDateTime());
                assertTrue(reader.isEnd());
                reader.close();
            }
        }
    }

    private static JSONReader[] readers(String text) {
        return new JSONReader[]{ascii(text), utf8(text),
                new JSONReaderUTF16(JSONFactory.createReadContext(), text, 0, text.length())};
    }

    private static JSONReaderUTF8 utf8(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new JSONReaderUTF8(JSONFactory.createReadContext(), bytes, 0, bytes.length);
    }

    private static JSONReaderASCII ascii(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.ISO_8859_1);
        return new JSONReaderASCII(JSONFactory.createReadContext(), text, bytes, 0, bytes.length);
    }
}
