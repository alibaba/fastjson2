package com.alibaba.fastjson2;

import com.alibaba.fastjson2.writer.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterUTF8JDK9Test {
    @Test
    public void test_writeString() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString((String) null);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void test_writeString_1() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString("a");
        assertEquals("\"a\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_2() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString("\"\"");
        assertEquals("\"\\\"\\\"\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_3() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString("abc");
        assertEquals("\"abc\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_4() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString("abcdefghijklmn01234567890");
        assertEquals("\"abcdefghijklmn01234567890\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_utf8() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString("中国");
        assertEquals("\"中国\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_utf8_1() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString("^á");
        assertEquals("\"^á\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_utf82() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 512; i++) {
            char ch = (char) i;
            buf.append(ch);
        }
        buf.append('á');
        String origin = buf.toString();

        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
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
    public void test_writeString_utf8_3() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());

        JSONObject map = new JSONObject();
        map.put("^á", 0);

        ObjectWriter objectWriter = JSONFactory.defaultObjectWriterProvider.getObjectWriter(JSONObject.class);
        objectWriter.write(jsonWriter, map);

        assertEquals("{\"^á\":0}", jsonWriter.toString());
    }

    @Test
    public void test_writeString_special() {
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString("\r\n\t\f\b\"");
        assertEquals("\"\\r\\n\\t\\f\\b\\\"\"", jsonWriter.toString());
    }

    @Test
    public void test_writeString_large() {
        char[] chars = new char[2048];
        Arrays.fill(chars, 'a');
        JSONWriterUTF8JDK9 jsonWriter = new JSONWriterUTF8JDK9(JSONFactory.createWriteContext());
        jsonWriter.writeString(new String(chars));
        assertEquals(chars.length + 2, jsonWriter.toString().length());
    }
}
