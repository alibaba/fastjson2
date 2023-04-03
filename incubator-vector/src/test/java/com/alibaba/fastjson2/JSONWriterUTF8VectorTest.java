package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterUTF8VectorTest {
    @Test
    public void test() {
        JSONWriter jsonWriter = (JSONWriter) new JSONWriterUTF8Vector.Factory().apply(JSONFactory.createWriteContext());
        jsonWriter.writeString("01234567890012345678900123456789001234567890");
        assertEquals("\"01234567890012345678900123456789001234567890\"", jsonWriter.toString());
    }

    @Test
    public void testUTF16() {
        JSONWriter jsonWriter = (JSONWriter) new JSONWriterUTF8Vector.Factory().apply(JSONFactory.createWriteContext());
        jsonWriter.writeString("01234567890012345678900123456789001234567890中国");
        assertEquals("\"01234567890012345678900123456789001234567890中国\"", jsonWriter.toString());
    }

    @Test
    public void writeStringLatin1() {
        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        JSONWriter jsonWriter = (JSONWriter) new JSONWriterUTF8Vector.Factory().apply(JSONFactory.createWriteContext());
        jsonWriter.writeStringLatin1(bytes);
        String json = jsonWriter.toString();
        String str = new String(bytes, 0, bytes.length, StandardCharsets.ISO_8859_1);
        Object parse = JSON.parse(json);
        assertEquals(str, parse);
    }
}
