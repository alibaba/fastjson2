package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;

import static com.alibaba.fastjson2.JSONWriter.Feature.NullAsDefaultValue;
import static com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        final int COUNT = 2048;
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
        final int COUNT = 2048;
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

    @Test
    public void test_writeRaw() {
        {
            JSONWriter writer = JSONWriter.ofUTF8();
            writer.writeRaw('1', '2');
            assertEquals("12", writer.toString());
        }
        {
            JSONWriter writer = JSONWriter.ofUTF8(PrettyFormat);
            writer.writeRaw('1', '2');
            assertEquals("12", writer.toString());
            assertEquals(
                    "12",
                    new String(writer.getBytes(StandardCharsets.UTF_8))
            );
            assertEquals(2, writer.size());
        }
        {
            JSONWriter writer = JSONWriter.ofUTF8();
            int size = 1000000;
            for (int i = 0; i < size; i++) {
                writer.writeRaw('1', '1');
            }
            char[] chars = new char[size * 2];
            Arrays.fill(chars, '1');
            assertEquals(new String(chars), writer.toString());
        }
        assertThrows(Exception.class, () -> JSONWriter.ofUTF8().writeRaw('中', '中'));
        assertThrows(Exception.class, () -> JSONWriter.ofUTF8().writeRaw('中'));
    }

    @Test
    public void test_writeDateTime14() {
        Bean bean = new Bean();
        bean.date = new Date(1679826319000L);
        byte[] bytes = JSON.toJSONBytes(bean);
        byte[] bytes2 = JSON.toJSONBytes(bean, PrettyFormat);
        Bean bean1 = JSON.parseObject(bytes, Bean.class);
        Bean bean2 = JSON.parseObject(bytes2, Bean.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    public static class Bean {
        @JSONField(format = "yyyyMMddHHmmss")
        public Date date;
    }

    @Test
    public void test_writeDateTime8() {
        Bean1 bean = new Bean1();
        bean.date = new Date(1679760000000L);
        byte[] bytes = JSON.toJSONBytes(bean);
        byte[] bytes2 = JSON.toJSONBytes(bean, PrettyFormat);
        Bean1 bean1 = JSON.parseObject(bytes, Bean1.class);
        Bean1 bean2 = JSON.parseObject(bytes2, Bean1.class);
        assertEquals(bean.date.getTime(), bean1.date.getTime());
        assertEquals(bean.date.getTime(), bean2.date.getTime());
    }

    public static class Bean1 {
        @JSONField(format = "yyyyMMdd")
        public Date date;
    }

    @Test
    public void test_writeLocalDateTime() {
        Bean2 bean = new Bean2();
        bean.dateTime = LocalDateTime.now();
        byte[] bytes = JSON.toJSONBytes(bean);
        byte[] bytes2 = JSON.toJSONBytes(bean, PrettyFormat);
        Bean2 bean1 = JSON.parseObject(bytes, Bean2.class);
        Bean2 bean2 = JSON.parseObject(bytes2, Bean2.class);
        assertEquals(bean.dateTime, bean1.dateTime);
        assertEquals(bean.dateTime, bean2.dateTime);
    }

    public static class Bean2 {
        public LocalDateTime dateTime;
    }

    @Test
    public void test_writeZonedDateTime() {
        Bean3 bean = new Bean3();
        bean.dateTime = ZonedDateTime.now();
        byte[] bytes = JSON.toJSONBytes(bean);
        byte[] bytes2 = JSON.toJSONBytes(bean, PrettyFormat);
        Bean3 bean1 = JSON.parseObject(bytes, Bean3.class);
        Bean3 bean2 = JSON.parseObject(bytes2, Bean3.class);
        assertEquals(bean.dateTime, bean1.dateTime);
        assertEquals(bean.dateTime, bean2.dateTime);

        Bean3 bean3 = JSONB.parseObject(JSON.parseObject(bytes).toJSONBBytes(), Bean3.class);
        assertEquals(bean.dateTime, bean3.dateTime);
    }

    public static class Bean3 {
        public ZonedDateTime dateTime;
    }

    @Test
    public void test_writeLocalTime() {
        Bean4 bean = new Bean4();
        bean.time = LocalTime.of(18, 38, 1, 423000000);
        byte[] bytes = JSON.toJSONBytes(bean);
        byte[] bytes2 = JSON.toJSONBytes(bean, PrettyFormat);
        assertEquals("{\"time\":\"18:38:01.423\"}", new String(bytes));
        Bean4 bean1 = JSON.parseObject(bytes, Bean4.class);
        Bean4 bean2 = JSON.parseObject(bytes2, Bean4.class);
        assertEquals(bean.time, bean1.time);
        assertEquals(bean.time, bean2.time);
    }

    public static class Bean4 {
        public LocalTime time;
    }

    @Test
    public void writeChars() {
        char[] chars = new char[256];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) i;
        }
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
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
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString(chars, 256, 512);
        String json = jsonWriter.toString();
        assertEquals(new String(chars, 256, 512), JSON.parse(json));
    }

    @Test
    public void writeCharsNull() {
        JSONWriterUTF8 jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeString((char[]) null);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void writeCharsNull1() {
        JSONWriter jsonWriter = new JSONWriterUTF8(
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
        JSONWriter jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
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
        JSONWriter jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext(JSONWriter.Feature.PrettyFormat));
        jsonWriter.writeStringLatin1(bytes);
        String json = jsonWriter.toString();
        String str = new String(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
        Object parse = JSON.parse(json);
        assertEquals(str, parse);
    }
}
