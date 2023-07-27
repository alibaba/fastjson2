package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import static com.alibaba.fastjson2.JSONWriter.Feature.NullAsDefaultValue;
import static com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JSONWriterUTF16Test {
    @Test
    public void test_write() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeBool(true);
        }
    }

    @Test
    public void test_writeNameRaw() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeNameRaw(new char[]{'a'});
        }
    }

    @Test
    public void test_writeNameRaw_1() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeNameRaw(new char[]{'a'}, 0, 1);
        }
    }

    @Test
    public void testWriterString0() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeString((String) null);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void testWriterString1() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.NullAsDefaultValue));
        jsonWriter.writeString((String) null);
        assertEquals("\"\"", jsonWriter.toString());
    }

    @Test
    public void testWriterString2() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.NullAsDefaultValue));
        jsonWriter.writeString("abcdefghijk1234567890abcdefghijk1234567890");
        assertEquals("\"abcdefghijk1234567890abcdefghijk1234567890\"", jsonWriter.toString());
    }

    @Test
    public void testWriterString3() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.NullAsDefaultValue));
        jsonWriter.writeString("abcdefghijk1234567890\\\"abcdefghijk1234567890");
        assertEquals("\"abcdefghijk1234567890\\\\\\\"abcdefghijk1234567890\"", jsonWriter.toString());
    }

    @Test
    public void writeDouble() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeDoubleArray(1, 2);
        assertEquals("[1.0,2.0]", jsonWriter.toString());
    }

    @Test
    public void writeColon() {
        final int COUNT = 100_000;
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        final int COUNT = 2048;
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        final int COUNT = 2048;
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeRaw(new char[]{','});
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeNameRaw(new char[0]);
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeNameRaw(new char[0], 0, 0);
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(',', string.charAt(i));
        }
    }

    @Test
    public void writeInt64() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
        jsonWriter.startArray();
        jsonWriter.writeInt64(9007199254740992L);
        jsonWriter.writeComma();
        jsonWriter.writeInt64(-9007199254740992L);
        jsonWriter.endArray();
        assertEquals("[\"9007199254740992\",\"-9007199254740992\"]", jsonWriter.toString());
    }

    @Test
    public void writeInt64_1() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.writeReference("$");
        assertEquals("{\"$ref\":\"$\"}", writer.toString());
        writer.chars = Arrays.copyOf(writer.chars, 23);
        writer.writeReference("中");
        assertEquals("{\"$ref\":\"$\"}{\"$ref\":\"中\"}", writer.toString());
        writer.writeReference("1234567890");
        assertEquals("{\"$ref\":\"$\"}{\"$ref\":\"中\"}{\"$ref\":\"1234567890\"}", writer.toString());
    }

    @Test
    public void writeString() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.writeString("中");
        assertEquals("\"中\"", writer.toString());
        writer.chars = Arrays.copyOf(writer.chars, 3);
        writer.writeString("中");
        assertEquals("\"中\"\"中\"", writer.toString());
        writer.writeString("1234567890");
        assertEquals("\"中\"\"中\"\"1234567890\"", writer.toString());
    }

    @Test
    public void startObject1() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.chars = Arrays.copyOf(writer.chars, 0);
        writer.startObject();
        assertEquals("{", writer.toString());
    }

    @Test
    public void startArray1() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.chars = Arrays.copyOf(writer.chars, 0);
        writer.startArray();
        assertEquals("[", writer.toString());
    }

    @Test
    public void writeColon1() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.chars = Arrays.copyOf(writer.chars, 0);
        writer.writeColon();
        assertEquals(":", writer.toString());
    }

    @Test
    public void writeComma1() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.chars = Arrays.copyOf(writer.chars, 0);
        writer.writeComma();
        assertEquals(",", writer.toString());
    }

    @Test
    public void testWrite0() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.chars = Arrays.copyOf(writer.chars, 0);
        writer.write0(':');
        assertEquals(":", writer.toString());
    }

    @Test
    public void endObject1() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.chars = Arrays.copyOf(writer.chars, 0);
        writer.endObject();
        assertEquals("}", writer.toString());
    }

    @Test
    public void endArray1() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.chars = Arrays.copyOf(writer.chars, 0);
        writer.endArray();
        assertEquals("]", writer.toString());
    }

    @Test
    public void writeDecimal() {
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
            writer.writeDecimal(null);
            assertEquals("null", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
            writer.writeDecimal(BigDecimal.valueOf(-9007199254740992L));
            assertEquals("-9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
            writer.writeDecimal(BigDecimal.valueOf(9007199254740992L));
            assertEquals("9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
            writer.writeDecimal(BigDecimal.valueOf(-9007199254740992L));
            assertEquals("\"-9007199254740992\"", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
            writer.writeDecimal(BigDecimal.valueOf(9007199254740992L));
            assertEquals("\"9007199254740992\"", writer.toString());
        }
    }

    @Test
    public void writeBigInt() {
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
            writer.writeBigInt(null, 0);
            assertEquals("null", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
            writer.writeBigInt(BigInteger.valueOf(-9007199254740992L), 0);
            assertEquals("-9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
            writer.writeBigInt(BigInteger.valueOf(9007199254740992L), 0);
            assertEquals("9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
            writer.writeBigInt(BigInteger.valueOf(-9007199254740992L), 0);
            assertEquals("\"-9007199254740992\"", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
            writer.writeBigInt(BigInteger.valueOf(9007199254740992L), 0);
            assertEquals("\"9007199254740992\"", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
            writer.chars = new char[0];
            writer.writeBigInt(BigInteger.valueOf(-9007199254740992L), 0);
            assertEquals("-9007199254740992", writer.toString());
        }
        {
            JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
            writer.chars = new char[16];
            writer.writeBigInt(BigInteger.valueOf(-9007199254740992L), 0);
            assertEquals("-9007199254740992", writer.toString());
        }
    }

    @Test
    public void writeUUID() {
        JSONWriterUTF16 writer = new JSONWriterUTF16(JSONFactory.createWriteContext());
        writer.writeUUID(null);
        assertEquals("null", writer.toString());
    }

    @Test
    public void test_writeRaw() {
        {
            JSONWriter writer = JSONWriter.ofUTF16();
            writer.writeRaw('1', '2');
            assertEquals("12", writer.toString());
        }
        {
            JSONWriter writer = JSONWriter.ofUTF16(PrettyFormat);
            writer.writeRaw('1', '2');
            assertEquals("12", writer.toString());
            assertEquals(
                    "12",
                    new String(writer.getBytes(StandardCharsets.UTF_8))
            );
            assertEquals(2, writer.size());
        }
        {
            JSONWriter writer = JSONWriter.ofUTF16();
            int size = 1000000;
            for (int i = 0; i < size; i++) {
                writer.writeRaw('1', '1');
            }
            char[] chars = new char[size * 2];
            Arrays.fill(chars, '1');
            assertEquals(new String(chars), writer.toString());
        }
    }

    @Test
    public void test_writeDateTime14() {
        Bean bean = new Bean();
        bean.date = new Date(1679826319000L);
        String str;
        JSONWriter jsonWriter = JSONWriter.ofUTF16();
        jsonWriter.writeAny(bean);
        str = jsonWriter.toString();

        assertEquals(StandardCharsets.UTF_16, jsonWriter.getCharset());
        assertFalse(jsonWriter.isUseSingleQuotes());

        String str2;
        JSONWriter jsonWriter1 = JSONWriter.ofUTF16(PrettyFormat);
        jsonWriter1.writeAny(bean);
        str2 = jsonWriter1.toString();

        Bean bean1 = JSON.parseObject(str, Bean.class);
        Bean bean2 = JSON.parseObject(str2, Bean.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    public static class Bean {
        @JSONField(format = "yyyyMMddHHmmss")
        public Date date;
    }

    @Test
    public void writeChars() {
        char[] chars = new char[256];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) i;
        }
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeString(chars);
        String json = jsonWriter.toString();
        assertEquals(new String(chars), JSON.parse(json));
    }

    @Test
    public void writeChars1() {
        char[] chars = new char[1024];
        Arrays.fill(chars, 'A');
        for (int i = 256; i < 768; i++) {
            chars[i] = (char) (i - 256);
        }
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeString(chars, 256, 512);
        String json = jsonWriter.toString();
        assertEquals(new String(chars, 256, 512), JSON.parse(json));
    }

    @Test
    public void writeCharsNull() {
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeString((char[]) null);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void writeCharsNull1() {
        JSONWriter jsonWriter =
                new JSONWriterUTF16(
                        JSONFactory.createWriteContext(NullAsDefaultValue, PrettyFormat)
                );
        jsonWriter.writeString((char[]) null);
        assertEquals("\"\"", jsonWriter.toString());
    }

    @Test
    public void writeStringLatin1() {
        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeStringLatin1(bytes);
        String json = jsonWriter.toString();
        String str = new String(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
        Object parse = JSON.parse(json);
        assertEquals(str, parse);
    }

    @Test
    public void writeStringLatin1Pretty() {
        byte[] bytes = new byte[1024 * 128];
        Arrays.fill(bytes, (byte) '\\');
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat));
        jsonWriter.writeStringLatin1(bytes);
        String json = jsonWriter.toString();
        String str = new String(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
        Object parse = JSON.parse(json);
        assertEquals(str, parse);
    }

    @Test
    public void testHex256() {
        if (BIG_ENDIAN) {
            return;
        }

        char[] buf = new char[4];
        int b0 = 0xab, b1 = 0xcd;
        int[] hex256 = JSONWriterUTF16.HEX256;
        long v = hex256[b0 & 0xff] | (((long) hex256[b1 & 0xff]) << 32);
        UNSAFE.putLong(
                buf,
                ARRAY_CHAR_BASE_OFFSET,
                v
        );
        assertEquals("abcd", new String(buf));
    }

    @Test
    public void testLHex256BigEndian() {
        if (!BIG_ENDIAN) {
            return;
        }

        char[] buf = new char[4];
        int b0 = 0xab, b1 = 0xcd;
        int[] hex256 = JSONWriterUTF16.HEX256.clone();
        for (int i = 0; i < hex256.length; i++) {
            hex256[i] = Integer.reverseBytes(hex256[i] << 8);
        }
        long v = hex256[b0 & 0xff]
                | (((long) hex256[b1 & 0xff]) << 32);
        UNSAFE.putLong(
                buf,
                ARRAY_CHAR_BASE_OFFSET,
                Long.reverseBytes(v << 8)
        );
        assertEquals("abcd", new String(buf));
    }
}
