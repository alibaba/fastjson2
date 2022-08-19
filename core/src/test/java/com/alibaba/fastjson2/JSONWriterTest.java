package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    @Test
    public void testUTF16GetBytes() {
        Charset charset = StandardCharsets.UTF_8;

        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeString("abc");
            assertEquals("\"abc\"", new String(jsonWriter.getBytes(), charset));
        }
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeString("中文");
            assertEquals("\"中文\"", new String(jsonWriter.getBytes(), charset));
        }
    }

    @Test
    public void unSupportedUTF16() {
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        assertNull(jsonWriter.getSymbolTable());

        assertThrows(JSONException.class, () -> jsonWriter.writeRaw((byte) 0));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new byte[0], 0, 0));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new byte[0], 0L));
        assertThrows(JSONException.class, () -> jsonWriter.startArray(0));
        assertThrows(JSONException.class, () -> jsonWriter.startArray(null, 0));
        assertThrows(JSONException.class, () -> jsonWriter.flushTo((OutputStream) null));
        assertThrows(JSONException.class, () -> jsonWriter.flushTo(null, null));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new byte[0]));
        assertThrows(JSONException.class, () -> jsonWriter.writeRaw(new byte[0]));
    }

    @Test
    public void unSupportedUTF8() {
        JSONWriter jsonWriter = new JSONWriterUTF8(JSONFactory.createWriteContext());
        assertThrows(JSONException.class, () -> jsonWriter.writeRaw(new char[0]));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new char[0]));
        assertThrows(JSONException.class, () -> jsonWriter.writeNameRaw(new char[0], 0, 0));
        assertThrows(JSONException.class, () -> jsonWriter.writeTypeName(""));
        assertThrows(JSONException.class, () -> jsonWriter.writeTypeName(new byte[0], 0));
    }

    @Test
    public void writeMillis() throws Exception {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeMillis(1);
        assertEquals("1", jsonWriter.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jsonWriter.flushTo(out);
        assertEquals("1", new String(out.toByteArray()));
    }

    @Test
    public void writeMillis1() throws Exception {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeMillis(101);
        assertEquals("101", jsonWriter.toString());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        jsonWriter.flushTo(out, StandardCharsets.UTF_8);
        assertEquals("101", new String(out.toByteArray()));
    }

    @Test
    public void writeInt64() {
        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.writeInt64(null);
        assertEquals("null", jsonWriter.toString());

        StringWriter writer = new StringWriter();
        jsonWriter.flushTo(writer);
        assertEquals("null", writer.toString());
    }

    @Test
    public void writeInstant() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.PrettyFormat);
        jsonWriter.writeInstant(null);
        assertEquals("null", jsonWriter.toString());

        StringWriter writer = new StringWriter();
        jsonWriter.flushTo(writer);
        assertEquals("null", writer.toString());
    }

    @Test
    public void writeSymbol() {
        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeSymbol("id");
        assertEquals("\"id\"", jsonWriter.toString());

        StringWriter writer = new StringWriter();
        jsonWriter.flushTo(writer);
        assertEquals("\"id\"", writer.toString());
    }

    @Test
    public void propertyPreFilter() {
        JSONWriter jsonWriter = JSONWriter.of();
        JSONWriter.Context context = jsonWriter.getContext();

        context.setPropertyPreFilter(null);
        assertNull(context.getPropertyPreFilter());

        context.setNameFilter(null);
        assertNull(context.getNameFilter());

        context.setValueFilter(null);
        assertNull(context.getValueFilter());

        context.setPropertyFilter(null);
        assertNull(context.getPropertyFilter());

        context.setContextValueFilter(null);
        assertNull(context.getContextValueFilter());

        context.setContextNameFilter(null);
        assertNull(context.getContextNameFilter());

        context.setAfterFilter(null);
        assertNull(context.getAfterFilter());

        context.setBeforeFilter(null);
        assertNull(context.getBeforeFilter());

        context.setLabelFilter(null);
        assertNull(context.getLabelFilter());
    }

    @Test
    public void path() {
        assertEquals(JSONWriter.Path.ROOT_0, JSONWriter.Path.ROOT_0);
        assertEquals(JSONWriter.Path.ROOT_0.hashCode(), JSONWriter.Path.ROOT_0.hashCode());
        assertNotEquals(JSONWriter.Path.ROOT_0, JSONWriter.Path.ROOT);
        assertNotEquals(JSONWriter.Path.ROOT_0, JSONWriter.Path.ROOT_1);
    }

    @Test
    public void getBytes() {
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeChar('a');
        assertArrayEquals(new byte[] {'"', 'a', '"'}, jsonWriter.getBytes());
    }
}
