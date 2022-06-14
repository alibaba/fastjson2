package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class JSONWriterTest {
    @Test
    public void test_feature() {
        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.config(JSONWriter.Feature.WriteNulls, JSONWriter.Feature.BeanToArray);
        assertTrue(jsonWriter.isEnabled(JSONWriter.Feature.WriteNulls));
        jsonWriter.getContext().config(JSONWriter.Feature.WriteNulls, false);
        assertFalse(jsonWriter.isEnabled(JSONWriter.Feature.WriteNulls.mask));
        jsonWriter.getContext().config(JSONWriter.Feature.WriteNulls, true);
        assertTrue(jsonWriter.getContext().isEnabled(JSONWriter.Feature.WriteNulls));
    }

    @Test
    public void test_str_comma() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.of();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.writeComma();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals(',', str.charAt(i));
        }
    }

    @Test
    public void test_utf8_comma() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.writeComma();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals(',', str.charAt(i));
        }
    }

    @Test
    public void test_str_startArray() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.of();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.startArray();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('[', str.charAt(i));
        }
    }

    @Test
    public void test_utf8_startArray() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.startArray();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('[', str.charAt(i));
        }
    }

    @Test
    public void test_str_startObject() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.of();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.startObject();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('{', str.charAt(i));
        }
    }

    @Test
    public void test_utf8_startObject() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.startObject();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('{', str.charAt(i));
        }
    }

    @Test
    public void test_str_endObject() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.of();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.endObject();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('}', str.charAt(i));
        }
    }

    @Test
    public void test_utf8_endObject() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.endObject();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('}', str.charAt(i));
        }
    }

    @Test
    public void test_str_endArray() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.of();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.endArray();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals(']', str.charAt(i));
        }
    }

    @Test
    public void test_utf8_endArray() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.endArray();
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals(']', str.charAt(i));
        }
    }

    @Test
    public void test_str_writeInt32() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.of();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.writeInt32(0);
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('0', str.charAt(i));
        }
    }

    @Test
    public void test_utf8_writeInt32() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.writeInt32(0);
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('0', str.charAt(i));
        }
    }

    @Test
    public void test_str_writeInt64() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.of();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.writeInt64(0);
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('0', str.charAt(i));
        }
    }

    @Test
    public void test_utf8_writeInt64() {
        final int COUNT = 2000;
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        for (int i = 0; i < COUNT; ++i) {
            jsonWriter.writeInt64(0);
        }
        String str = jsonWriter.toString();
        assertEquals(COUNT, str.length());
        for (int i = 0; i < COUNT; ++i) {
            assertEquals('0', str.charAt(i));
        }
    }

    @Test
    public void test_utf8_writeDoubleArray() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeDouble(new double[]{0D, 1D});
        assertEquals("[0.0,1.0]", jsonWriter.toString());
    }

    @Test
    public void test_utf8_writeDoubleArray1() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONFactory.createWriteContext());
        jsonWriter.writeDoubleArray(0D, 1D);
        assertEquals("[0.0,1.0]", jsonWriter.toString());
    }

    @Test
    public void test_utf8_ref() {
        ArrayList list = new ArrayList();
        list.add(list);
        JSONObject object = JSONObject.of("values", list);
        JSONWriter jsonWriter = JSONWriter.ofUTF8(JSONWriter.Feature.ReferenceDetection);
        jsonWriter.writeAny(object);
        assertEquals("{\"values\":[{\"$ref\":\"values\"}]}", jsonWriter.toString());
    }

    @Test
    public void test_ref() {
        ArrayList list = new ArrayList();
        list.add(list);
        JSONObject object = JSONObject.of("values", list);
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.ReferenceDetection);
        jsonWriter.writeAny(object);
        assertEquals("{\"values\":[{\"$ref\":\"values\"}]}", jsonWriter.toString());
    }

    @Test
    public void test_base64() {
        byte[] bytes = new byte[1024];
        new Random().nextBytes(bytes);
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeBase64(bytes);
            String str = jsonWriter.toString();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            assertEquals(base64, str.substring(1, str.length() - 1));
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeBase64(bytes);
            String str = jsonWriter.toString();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            assertEquals(base64, str.substring(1, str.length() - 1));
        }
    }
}
