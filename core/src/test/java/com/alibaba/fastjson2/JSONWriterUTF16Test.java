package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.util.IOUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.*;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.BrowserSecure;
import static com.alibaba.fastjson2.JSONWriterUTF16.BYTE_VEC_64_DOUBLE_QUOTE;
import static com.alibaba.fastjson2.JSONWriterUTF16.BYTE_VEC_64_SINGLE_QUOTE;
import static com.alibaba.fastjson2.util.JDKUtils.*;
import static org.junit.jupiter.api.Assertions.*;

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
        String str = new String(bytes, StandardCharsets.ISO_8859_1);
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
        String str = new String(bytes, StandardCharsets.ISO_8859_1);
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

    @Test
    public void writeName8() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());

        char[] name = "a123".toCharArray();
        long nameValue = UNSAFE.getLong(name, ARRAY_CHAR_BASE_OFFSET);

        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName6Raw(nameValue);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName7Raw(nameValue);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName8Raw(nameValue);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName9Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName10Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName11Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName12Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName13Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName14Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName15Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName16Raw(nameValue, 1);
        }
    }

    @Test
    public void writeName8Pretty() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(PrettyFormat));
        jsonWriter.level += 2;

        char[] name = "a123".toCharArray();
        long nameValue = UNSAFE.getLong(name, ARRAY_CHAR_BASE_OFFSET);

        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName6Raw(nameValue);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName7Raw(nameValue);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName8Raw(nameValue);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName9Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName10Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName11Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName12Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName13Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName14Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName15Raw(nameValue, 1);
        }
        {
            jsonWriter.chars = new char[0];
            jsonWriter.off = 0;
            jsonWriter.writeName16Raw(nameValue, 1);
        }
    }

    @Test
    public void issue_2484() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());

        char[] name = "a123".toCharArray();
        long nameValue = UNSAFE.getLong(name, ARRAY_CHAR_BASE_OFFSET);
        byte PRETTY_NON = 0, PRETTY_TAB = 1, PRETTY_SPACE = 3;

        final int initOffset = 8183;
        for (int i = 0; i < 16; i++) {
            int initSize = 8196 - i;
            {
                jsonWriter.startObject = true;
                jsonWriter.pretty = PRETTY_NON;

                jsonWriter.chars = new char[initSize];
                jsonWriter.off = initOffset;
                jsonWriter.writeName7Raw(nameValue);
            }
            {
                jsonWriter.startObject = false;
                jsonWriter.pretty = PRETTY_NON;

                jsonWriter.chars = new char[initSize];
                jsonWriter.off = initOffset;
                jsonWriter.writeName7Raw(nameValue);
            }
            {
                jsonWriter.startObject = false;
                jsonWriter.pretty = PRETTY_TAB;

                jsonWriter.chars = new char[initSize];
                jsonWriter.off = initOffset;
                jsonWriter.writeName7Raw(nameValue);
            }
        }
    }

    @Test
    public void grow() {
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeNull();
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeReference("$.abc");
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeBase64(new byte[3]);
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeString(Arrays.asList("abc"));
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeStringLatin1(new byte[] {1, 2, 3});
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16(BrowserSecure);
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeStringLatin1(new byte[] {1, '>', '<'});
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16(BrowserSecure);
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeStringLatin1(new byte[] {'a', 'b', 'c'});
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeString(new char[] {'a', 'b', 'c'});
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeString(new char[] {'a', 'b', 'c'}, 0, 3);
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeUUID(UUID.randomUUID());
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeInt32(123);
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeInt8((byte) 123);
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeInt8(new byte[]{1, 2, 3});
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeInt16((short) 123);
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeInt32(new int[] {1, 2, 3});
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeListInt32(Arrays.asList(1, 2, null));
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeInt64(123L);
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeInt64(new long[] {1, 2, 3});
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeListInt64(Arrays.asList(1L, 2L, null));
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeFloat(123);
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeDouble(123L);
            }
            jsonWriter.close();
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeNameRaw(new char[] {'a', 'b', 'c'});
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeNameRaw(new char[] {'a', 'b', 'c'}, 0, 3);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeRaw(new char[] {'a', 'b', 'c'});
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeRaw('a', 'b');
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeDateTime14(2014, 4, 5, 6, 7, 8);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeDateTime19(2014, 4, 5, 6, 7, 8);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeLocalDate(LocalDate.MIN);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeLocalDateTime(LocalDateTime.MIN);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeDateYYYMMDD8(2014, 4, 5);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeDateYYYMMDD10(2014, 4, 5);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeTimeHHMMSS8(3, 4, 5);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeLocalTime(LocalTime.MIN);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            ZonedDateTime now = ZonedDateTime.now();
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeZonedDateTime(now);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            OffsetDateTime now = OffsetDateTime.now();
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeOffsetDateTime(now);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            OffsetTime now = OffsetTime.now();
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeOffsetTime(now);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            OffsetTime now = OffsetTime.now();
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeDateTimeISO8601(2014, 3, 4, 5, 6, 7, 8, 9, true);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeBigInt(new BigInteger("123456789012345678901234567890"), 0);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeDecimal(new BigDecimal("12345678901234567890.1234567890"), 0, new DecimalFormat("###.##"));
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.write(Arrays.asList(1));
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.write(Arrays.asList(1, 2));
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.write(Arrays.asList(1, 2, 3));
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.writeBool(true);
            }
        }
        {
            JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
            jsonWriter.chars = new char[0];
            for (int i = 0; i < 1000; i++) {
                jsonWriter.write(JSONObject.of());
            }
        }
    }

    @Test
    public void testEscaped() {
        char[] chars = new char[] {'"', '"', '"', '"'};
        byte[] bytes = toBytes(chars);

        JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
        jsonWriter.writeStringUTF16(bytes);
        String str = jsonWriter.toString();
        System.out.println(str);
        assertEquals("\"\\\"\\\"\\\"\\\"\"", str);
    }

    @Test
    public void testEscaped_1() {
        String str = "😀😉";
        char[] chars = str.toCharArray();
        byte[] bytes = toBytes(chars);

        JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
        jsonWriter.writeStringUTF16(bytes);
        assertEquals("\"" + str + "\"", jsonWriter.toString());
    }

    static boolean containsEscapedUTF16(long v, long quote) {
        /*
          for (int i = 0; i < 8; ++i) {
            byte c = (byte) data;
            if (c == quote || c == '\\' || c < ' ') {
                return true;
            }
            data >>>= 8;
          }
          return false;
         */
        long x22 = v ^ quote; // " -> 0x22, ' -> 0x27
        long x5c = v ^ 0x005C005C_005C005CL;

        x22 = (x22 - 0x00010001_00010001L) & ~x22;
        x5c = (x5c - 0x00010001_00010001L) & ~x5c;

        return ((x22 | x5c | (0x007F007F_007F007FL - v + 0x00100010_00100010L) | v) & 0x00800080_00800080L) != 0;
    }

    @Test
    public void testEscaped_2() {
        String str = "abcd";
        char[] chars = str.toCharArray();
        byte[] bytes = toBytes(chars);

        long v = IOUtils.getLongLE(bytes, 0);
        assertFalse(
                containsEscapedUTF16(
                        v,
                        BYTE_VEC_64_DOUBLE_QUOTE));

        JSONWriterUTF16 jsonWriter = (JSONWriterUTF16) JSONWriter.ofUTF16();
        jsonWriter.writeStringUTF16(bytes);
        assertEquals("\"" + str + "\"", jsonWriter.toString());
    }

    @Test
    public void test_containsEscapedUTF16() {
        char[] chars = new char[4];
        char[] specials_double = new char[]{'\\', '"', '\r', '\n'};
        char[] specials_single = new char[]{'\\', '\'', '\r', '\n'};
        {
            Arrays.fill(chars, 'A');
            {
                long v = IOUtils.getLongLE(
                        toBytes(chars),
                        0);
                assertFalse(containsEscapedUTF16(v, BYTE_VEC_64_DOUBLE_QUOTE));
            }

            {
                chars[0] = '"';
                long v = IOUtils.getLongLE(
                        toBytes(chars),
                        0);
                assertFalse(containsEscapedUTF16(v, BYTE_VEC_64_SINGLE_QUOTE));
                assertTrue(containsEscapedUTF16(v, BYTE_VEC_64_DOUBLE_QUOTE));
            }
            {
                chars[0] = '\'';
                long v = IOUtils.getLongLE(
                        toBytes(chars),
                        0);
                assertTrue(containsEscapedUTF16(v, BYTE_VEC_64_SINGLE_QUOTE));
                assertFalse(containsEscapedUTF16(v, BYTE_VEC_64_DOUBLE_QUOTE));
            }
        }
        {
            Arrays.fill(chars, '中');
            {
                long v = IOUtils.getLongLE(
                        toBytes(chars),
                        0);
                assertFalse(containsEscapedUTF16(v, BYTE_VEC_64_DOUBLE_QUOTE));
            }
        }
        {
            Arrays.fill(chars, '®');
            {
                long v = IOUtils.getLongLE(
                        toBytes(chars),
                        0);
                assertTrue(containsEscapedUTF16(v, BYTE_VEC_64_DOUBLE_QUOTE));
            }
        }

        for (int i = 0; i < 4; i++) {
            Arrays.fill(chars, 'A');
            for (char c : specials_double) {
                chars[i] = c;
                long v = IOUtils.getLongLE(
                        toBytes(chars),
                        0);
                assertTrue(containsEscapedUTF16(v, BYTE_VEC_64_DOUBLE_QUOTE));
            }
            for (char c : specials_single) {
                chars[i] = c;
                long v = IOUtils.getLongLE(
                        toBytes(chars),
                        0);
                assertTrue(containsEscapedUTF16(v, BYTE_VEC_64_SINGLE_QUOTE));
            }
        }
        {
            chars = new char[]{'a', 'b', 'c', 'd'};
            long v = IOUtils.getLongLE(
                    toBytes(chars),
                    0);
            assertFalse(containsEscapedUTF16(v, BYTE_VEC_64_SINGLE_QUOTE));
            assertFalse(containsEscapedUTF16(v, BYTE_VEC_64_DOUBLE_QUOTE));
        }
        {
            chars = new char[]{'A', 'B', 'C', 'D'};
            long v = IOUtils.getLongLE(
                    toBytes(chars),
                    0);
            assertFalse(containsEscapedUTF16(v, BYTE_VEC_64_SINGLE_QUOTE));
            assertFalse(containsEscapedUTF16(v, BYTE_VEC_64_DOUBLE_QUOTE));
        }
    }

    static byte[] toBytes(char[] chars) {
        if (chars.length != 4) {
            throw new UnsupportedOperationException();
        }
        byte[] bytes = new byte[chars.length << 1];
        long x = IOUtils.getLongLE(chars, 0);
        IOUtils.putLongLE(bytes, 0, x);
        return bytes;
    }

    @Test
    public void write2() {
        char[] chars = new char[4];
        JSONWriterUTF16.writeEscapedChar(chars, 0, '\r');
        JSONWriterUTF16.writeEscapedChar(chars, 2, '\n');
        assertEquals("\\r\\n", new String(chars));
    }

    @Test
    public void writeU4() {
        char[] chars = new char[6];
        JSONWriterUTF16.writeU4Hex2(chars, 0, 1);
        assertEquals("\\u0001", new String(chars));

        IOUtils.putLongUnaligned(chars, 2, IOUtils.utf16Hex4U(1));
        assertEquals("\\u0001", new String(chars));

        chars = new char[] {'0', '1', '2', '3'};
        assertEquals(0, IOUtils.getLongLE(chars, 0) & 0xFF00FF00FF00FF00L);

        chars = new char[] {'中', '1', '2', '3'};
        assertNotEquals(0, IOUtils.getLongLE(chars, 0) & 0xFF00FF00FF00FF00L);
    }

    @Test
    public void testUTF16() {
        char[] chars = new char[32];
        for (int i = 0; i < chars.length; i++) {
            {
                Arrays.fill(chars, 'A');
                chars[chars.length - 1] = '中';
                String str = new String(chars);
                String json = JSON.toJSONString(str);
                assertEquals(str, JSON.parse(json));
            }
            {
                Arrays.fill(chars, 'A');
                chars[chars.length - 1] = '中';
                chars[i] = '\r';
                String str = new String(chars);
                String json = JSON.toJSONString(str);
                assertEquals(str, JSON.parse(json));
            }

            {
                Arrays.fill(chars, '中');
                chars[i] = '\r';
                String str = new String(chars);
                String json = JSON.toJSONString(str);
                assertEquals(str, JSON.parse(json));
            }
        }
    }
}
