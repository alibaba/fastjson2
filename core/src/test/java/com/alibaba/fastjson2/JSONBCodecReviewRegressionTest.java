package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

public class JSONBCodecReviewRegressionTest {
    static byte[] string(byte type, Charset charset, String value) {
        try (JSONWriter writer = JSONWriter.ofJSONB()) {
            byte[] bytes = value.getBytes(charset);
            writer.writeRaw(type);
            writer.writeInt32(bytes.length);
            writer.writeRaw(bytes);
            writer.writeInt32(37);
            return writer.getBytes();
        }
    }

    @Test
    public void numericStringsKeepRangeAndFraction() {
        for (byte type : new byte[]{BC_STR_ASCII, BC_STR_UTF8, BC_STR_UTF16LE}) {
            Charset charset = type == BC_STR_UTF16LE ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_8;
            try (JSONReader reader = JSONReader.ofJSONB(string(type, charset, "4294967297"))) {
                assertEquals(4294967297L, reader.readInt64Value());
                assertEquals(37, reader.readInt32Value());
            }
            try (JSONReader reader = JSONReader.ofJSONB(string(type, charset, "12.75"))) {
                assertEquals(12.75F, reader.readFloatValue());
                assertEquals(37, reader.readInt32Value());
            }
            try (JSONReader reader = JSONReader.ofJSONB(string(type, charset, "12.75"))) {
                assertEquals(12.75D, reader.readDoubleValue());
                assertEquals(37, reader.readInt32Value());
            }
        }
        for (Object number : new Object[]{12.75F, 12.75D, new BigInteger("123456789012345678901234567890")}) {
            try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(number))) {
                assertEquals(new BigDecimal(number.toString()), reader.readBigDecimal());
            }
        }
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(new BigDecimal("12.75")))) {
            assertEquals(12.75D, reader.readDoubleValue());
        }
    }

    @Test
    public void unicodeHashesMatchCanonicalCharactersAndConsumeInput() {
        byte[] types = {BC_STR_ASCII, BC_STR_UTF8, BC_STR_UTF16LE, BC_STR_UTF16BE, BC_STR_UTF16};
        Charset[] charsets = {StandardCharsets.ISO_8859_1, StandardCharsets.UTF_8,
                StandardCharsets.UTF_16LE, StandardCharsets.UTF_16BE, StandardCharsets.UTF_16};
        for (int i = 0; i < types.length; ++i) {
            for (String value : new String[]{"", "abcd\u00e9", "\u00e9abcdefghi", "abcdefghi\u00e9", "Name__", "\u0000x"}) {
                byte[] bytes = string(types[i], charsets[i], value);
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(Fnv.hashCode64(value), reader.readValueHashCode(), value);
                    assertEquals(value, reader.getString());
                    assertEquals(37, reader.readInt32Value());
                }
                try (JSONReader reader = JSONReader.ofJSONB(bytes)) {
                    assertEquals(Fnv.hashCode64(value), reader.readFieldNameHashCode(), value);
                    assertEquals(Fnv.hashCode64LCase(value), reader.getNameHashCodeLCase(), value);
                    assertEquals(37, reader.readInt32Value());
                }
            }
        }
        for (byte type : new byte[]{BC_STR_UTF8, BC_STR_UTF16LE, BC_STR_UTF16BE}) {
            Charset charset = type == BC_STR_UTF8 ? StandardCharsets.UTF_8
                    : type == BC_STR_UTF16LE ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_16BE;
            String value = "\u4e2d\ud83d\ude00";
            try (JSONReader reader = JSONReader.ofJSONB(string(type, charset, value))) {
                assertEquals(Fnv.hashCode64(value), reader.readValueHashCode());
                assertEquals(37, reader.readInt32Value());
            }
        }
    }

    @Test
    public void externalTypeSymbolsHaveNegativeOrdinal() {
        String name = "example.ReviewType";
        SymbolTable table = JSONB.symbolTable(name);
        try (JSONWriter writer = JSONWriter.ofJSONB(table)) {
            writer.writeTypeName(name);
            writer.writeInt32(37);
            byte[] bytes = writer.getBytes();
            assertEquals(BC_TYPED_ANY, bytes[0]);
            assertEquals(-1, bytes[1]);
            try (JSONReader reader = JSONReader.ofJSONB(bytes, 1, bytes.length - 1, table)) {
                assertEquals(Fnv.hashCode64(name), reader.readTypeHashCode());
                assertEquals(name, reader.getString());
                assertEquals(37, reader.readInt32Value());
            }
            byte[] raw = new byte[64];
            int end = JSONB.IO.writeTypeName(raw, 0, name, writer);
            assertEquals(2, end);
            assertEquals(-1, raw[1]);
        }
    }

    @Test
    public void nullAndCharacterReadsKeepCursor() {
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes((Object) null))) {
            assertNull(reader.readArray());
        }
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes((Object) null))) {
            assertNull(reader.readBinary());
        }
        try (JSONWriter writer = JSONWriter.ofJSONB()) {
            writer.writeString("abc");
            writer.writeInt32(37);
            try (JSONReader reader = JSONReader.ofJSONB(writer.getBytes())) {
                assertEquals('a', reader.readCharValue());
                assertEquals(37, reader.readInt32Value());
            }
        }
        try (JSONReader reader = JSONReader.ofJSONB(string(BC_STR_UTF8, StandardCharsets.UTF_8, "N"))) {
            assertFalse(reader.readBoolValue());
        }
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes("x"))) {
            assertThrows(JSONException.class, reader::readBoolValue);
        }
    }

    @Test
    public void arraysPreserveLongTypesAndReferencePositions() {
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(10000L))) {
            assertEquals(Long.valueOf(10000), reader.readNumber());
        }
        try (JSONWriter writer = JSONWriter.ofJSONB()) {
            writer.startArray(3);
            writer.writeInt64(10000);
            writer.writeReference("$[0]");
            writer.writeString("tail");
            try (JSONReader reader = JSONReader.ofJSONB(writer.getBytes())) {
                List list = reader.readArray();
                reader.handleResolveTasks(list);
                assertEquals(Arrays.asList(10000L, 10000L, "tail"), list);
            }
        }
    }

    @Test
    public void skipEveryVariableNumericAndTimestampEncoding() {
        Object[] values = {'\u4e2d', BigInteger.valueOf(Long.MAX_VALUE),
                new BigInteger("123456789012345678901234567890"), Instant.ofEpochSecond(123, 456)};
        for (Object value : values) {
            try (JSONWriter writer = JSONWriter.ofJSONB()) {
                writer.writeAny(value);
                writer.writeInt32(37);
                try (JSONReader reader = JSONReader.ofJSONB(writer.getBytes())) {
                    reader.skipValue();
                    assertEquals(37, reader.readInt32Value(), value.toString());
                }
            }
        }
        try (JSONReader reader = JSONReader.ofJSONB(string(BC_STR_GB18030, Charset.forName("GB18030"), "\u4e2d"))) {
            reader.skipValue();
            assertEquals(37, reader.readInt32Value());
        }
    }

    @Test
    public void variableLengthDateAndContextZone() {
        for (byte type : new byte[]{BC_STR_ASCII, BC_STR_UTF8}) {
            try (JSONReader reader = JSONReader.ofJSONB(string(type, StandardCharsets.UTF_8, "2024-01-02"))) {
                assertEquals(LocalDate.of(2024, 1, 2), reader.readLocalDate());
                assertEquals(37, reader.readInt32Value());
            }
        }
        JSONReader.Context context = JSONFactory.createReadContext();
        context.setZoneId(ZoneOffset.ofHours(3));
        try (JSONReader reader = JSONReader.ofJSONB(context, JSONB.toBytes(Instant.ofEpochSecond(123, 456)))) {
            assertEquals(Instant.ofEpochSecond(123, 456).atZone(ZoneOffset.ofHours(3)), reader.readZonedDateTime());
        }
    }

    @Test
    public void emptyStringFeaturesDoNotReadTheFollowingValue() {
        for (byte type : new byte[]{BC_STR_UTF8, BC_STR_UTF16LE, BC_STR_UTF16BE}) {
            Charset charset = type == BC_STR_UTF8 ? StandardCharsets.UTF_8
                    : type == BC_STR_UTF16LE ? StandardCharsets.UTF_16LE : StandardCharsets.UTF_16BE;
            try (JSONReader reader = JSONReader.ofJSONB(string(type, charset, " "),
                    JSONReader.Feature.TrimString, JSONReader.Feature.EmptyStringAsNull)) {
                assertNull(reader.readString());
                assertEquals(37, reader.readInt32Value());
            }
        }
    }

    @Test
    public void hexBinaryAndUuidValidation() {
        byte[] expected = {(byte) 0xab, (byte) 0xcd};
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes("aBcD"))) {
            assertArrayEquals(expected, reader.readHex());
        }
        try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(expected))) {
            assertArrayEquals(expected, reader.readHex());
        }
        for (String value : new String[]{"A", "GG"}) {
            try (JSONReader reader = JSONReader.ofJSONB(JSONB.toBytes(value))) {
                assertThrows(JSONException.class, reader::readHex);
            }
        }
        try (JSONReader reader = JSONReader.ofJSONB(new byte[]{BC_BINARY, 16, 1})) {
            assertThrows(JSONException.class, reader::readUUID);
        }
    }

    @Test
    public void writerReservesFullLengthPrefixes() {
        try (JSONWriterJSONB writer = (JSONWriterJSONB) JSONWriter.ofJSONB()) {
            writer.bytes = new byte[1];
            writer.writeString("hello".toCharArray(), 0, 5, true);
            assertEquals("hello", JSONB.parseObject(writer.getBytes(), String.class));
        }
        int size = 262144;
        try (JSONWriterJSONB writer = (JSONWriterJSONB) JSONWriter.ofJSONB()) {
            writer.bytes = new byte[size + 5];
            writer.writeBool(new boolean[size]);
            assertEquals(size + 6, writer.size());
            assertEquals(size, JSONB.parseObject(writer.getBytes(), boolean[].class).length);
        }
        try (JSONWriterJSONB writer = (JSONWriterJSONB) JSONWriter.ofJSONB()) {
            writer.symbolIndex = 100;
            byte[] name = JSONB.toBytes("Review");
            writer.bytes = new byte[name.length + 2];
            writer.writeTypeName(name, Fnv.hashCode64("Review"));
            assertEquals(name.length + 3, writer.size());
        }
    }

    @Test
    public void skippedSymbolsRemainDefined() {
        try (JSONWriter writer = JSONWriter.ofJSONB(JSONWriter.Feature.WriteNameAsSymbol)) {
            writer.writeName("symbolName");
            writer.writeName("symbolName");
            try (JSONReader reader = JSONReader.ofJSONB(writer.getBytes())) {
                reader.skipName();
                assertEquals("symbolName", reader.readFieldName());
            }
        }
    }
}
