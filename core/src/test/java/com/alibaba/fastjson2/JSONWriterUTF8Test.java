package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

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
}
