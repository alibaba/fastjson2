package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterUTF8Test {
    @Test
    public void test_writeString() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString((String) null);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void test_writeString_1() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString("a");
        assertEquals("\"a\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_2() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString("\"\"");
        assertEquals("\"\\\"\\\"\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_3() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString("abc");
        assertEquals("\"abc\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_4() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString("abcdefghijklmn01234567890");
        assertEquals("\"abcdefghijklmn01234567890\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_utf8() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString("中国");
        assertEquals("\"中国\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_utf8_1() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString("^á");
        assertEquals("\"^á\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_utf8_2() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 512; i++) {
            char ch = (char) i;
            buf.append(ch);
        }
        String origin = buf.toString();

        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString(origin);
        String json = jsonWriter.toString();
        String str = (String) JSON.parse(json);
        assertEquals(origin.length(), str.length());
        for (int i = 0; i < origin.length(); i++) {
            assertEquals(origin.charAt(i), str.charAt(i));
        }
        assertEquals(origin, str);
    }

    @Test
    public void test_writeString_special() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString("\r\n\t\f\b\"");
        assertEquals("\"\\r\\n\\t\\f\\b\\\"\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_large() {
        char[] chars = new char[2048];
        Arrays.fill(chars, 'a');
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString(new String(chars));
        assertEquals(chars.length + 2, jsonWriter.toString().length());
    }

    @Test
    public void writeRaw() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeRaw('A');
        assertEquals("A", jsonWriter.toString());
    }

    @Test
    public void writeRaw1() {
        String str = "\"abc\":";
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);

        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.startObject();
        jsonWriter.writeNameRaw(utf8, 0, utf8.length);
        assertEquals("{\"abc\":", jsonWriter.toString());
    }

    @Test
    public void writeLocalDate() {
        LocalDate localDate = LocalDate.of(2018, 6, 23);
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeLocalDate(localDate);
        assertEquals("\"2018-06-23\"", jsonWriter.toString());
    }

    @Test
    public void writeColon() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeColon();
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(':', string.charAt(i));
        }
    }

    @Test
    public void write0() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.write0(':');
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(':', string.charAt(i));
        }
    }

    @Test
    public void startObject() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.startObject();
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals('{', string.charAt(i));
        }
    }

    @Test
    public void endObject() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.endObject();
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals('}', string.charAt(i));
        }
    }

    @Test
    public void startArray() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.startArray();
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals('[', string.charAt(i));
        }
    }

    @Test
    public void endArray() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.endArray();
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(']', string.charAt(i));
        }
    }

    @Test
    public void writeRawLarge() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeRaw(",");
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(',', string.charAt(i));
        }
    }

    @Test
    public void writeRawLarge1() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeRaw(new byte[]{','});
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(',', string.charAt(i));
        }
    }

    @Test
    public void writeRawLarge2() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeRaw(',');
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(',', string.charAt(i));
        }
    }

    @Test
    public void writeNameRawLarge() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeNameRaw(new byte[0]);
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(',', string.charAt(i));
        }
    }

    @Test
    public void writeNameRawLarge1() {
        final int COUNT = 100_000;
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeNameRaw(new byte[0], 0, 0);
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(',', string.charAt(i));
        }
    }

    @Test
    public void writeInt64() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
        jsonWriter.startArray();
        jsonWriter.writeInt64(9007199254740992L);
        jsonWriter.writeComma();
        jsonWriter.writeInt64(-9007199254740992L);
        jsonWriter.endArray();
        assertEquals("[\"9007199254740992\",\"-9007199254740992\"]", jsonWriter.toString());
    }

    @Test
    public void writeInt64_1() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.startArray();
        jsonWriter.writeInt64(Long.MIN_VALUE);
        jsonWriter.writeComma();
        jsonWriter.writeInt64(Long.MIN_VALUE);
        jsonWriter.writeComma();
        jsonWriter.writeInt64(9007199254740992L);
        jsonWriter.writeComma();
        jsonWriter.writeInt64(-9007199254740992L);
        jsonWriter.endArray();
        assertEquals("[-9223372036854775808,-9223372036854775808,9007199254740992,-9007199254740992]", jsonWriter.toString());
    }

    @Test
    public void testWriteReference() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.writeReference("$");
        assertEquals("{\"$ref\":\"$\"}", writer.toString());
        writer.bytes = Arrays.copyOf(writer.bytes, 25);
        writer.writeReference("中");
        assertEquals("{\"$ref\":\"$\"}{\"$ref\":\"中\"}", writer.toString());
    }

    @Test
    public void startObject1() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.bytes = Arrays.copyOf(writer.bytes, 0);
        writer.startObject();
        assertEquals("{", writer.toString());
    }

    @Test
    public void startArray1() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.bytes = Arrays.copyOf(writer.bytes, 0);
        writer.startArray();
        assertEquals("[", writer.toString());
    }

    @Test
    public void writeColon1() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.bytes = Arrays.copyOf(writer.bytes, 0);
        writer.writeColon();
        assertEquals(":", writer.toString());
    }

    @Test
    public void writeComma1() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.bytes = Arrays.copyOf(writer.bytes, 0);
        writer.writeComma();
        assertEquals(",", writer.toString());
    }

    @Test
    public void testWrite0() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.bytes = Arrays.copyOf(writer.bytes, 0);
        writer.write0(':');
        assertEquals(":", writer.toString());
    }

    @Test
    public void endObject1() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.bytes = Arrays.copyOf(writer.bytes, 0);
        writer.endObject();
        assertEquals("}", writer.toString());
    }

    @Test
    public void endArray1() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.bytes = Arrays.copyOf(writer.bytes, 0);
        writer.endArray();
        assertEquals("]", writer.toString());
    }

    @Test
    public void writeDecimal() {
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.writeDecimal(null);
            assertEquals("null", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.writeDecimal(BigDecimal.valueOf(-9007199254740992L));
            assertEquals("-9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.writeDecimal(BigDecimal.valueOf(9007199254740992L));
            assertEquals("9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
            writer.writeDecimal(BigDecimal.valueOf(-9007199254740992L));
            assertEquals("\"-9007199254740992\"", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
            writer.writeDecimal(BigDecimal.valueOf(9007199254740992L));
            assertEquals("\"9007199254740992\"", writer.toString());
        }
    }

    @Test
    public void writeBigInt() {
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.writeBigInt(null, 0);
            assertEquals("null", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.writeBigInt(BigInteger.valueOf(-9007199254740992L), 0);
            assertEquals("-9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.writeBigInt(BigInteger.valueOf(9007199254740992L), 0);
            assertEquals("9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
            writer.writeBigInt(BigInteger.valueOf(-9007199254740992L), 0);
            assertEquals("\"-9007199254740992\"", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
            writer.writeBigInt(BigInteger.valueOf(9007199254740992L), 0);
            assertEquals("\"9007199254740992\"", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.bytes = new byte[0];
            writer.writeBigInt(BigInteger.valueOf(-9007199254740992L), 0);
            assertEquals("-9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.bytes = new byte[16];
            writer.writeBigInt(BigInteger.valueOf(-9007199254740992L), 0);
            assertEquals("-9007199254740992", writer.toString());
        }
    }

    @Test
    public void writeRaw2() {
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.bytes = new byte[0];
            writer.writeRaw("中国ā");
            assertEquals("中国ā", writer.toString());
        }
        {
            JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
            writer.bytes = new byte[2];
            writer.writeRaw("中国ā");
            assertEquals("中国ā", writer.toString());
        }
    }

    @Test
    public void writeUUID() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext());
        writer.writeUUID(null);
        assertEquals("null", writer.toString());
    }

    @Test
    public void testNoneStringAsString() {
        JSONWriterUTF8 writer = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.WriteNonStringValueAsString));

        writer.startArray();
        writer.writeFloat(1);
        writer.writeComma();
        writer.writeDouble(2);
        writer.writeComma();
        writer.writeFloat(new float[]{3, 4});
        writer.writeComma();
        writer.writeDouble(new double[]{5, 6});
        writer.endArray();

        assertEquals("[\"1.0\",\"2.0\",[\"3.0\",\"4.0\"],[\"5.0\",\"6.0\"]]", writer.toString());
    }
}
