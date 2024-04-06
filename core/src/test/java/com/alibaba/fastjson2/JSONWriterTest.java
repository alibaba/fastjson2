package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JDKUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;
import static com.alibaba.fastjson2.JSONWriter.Path.ROOT;
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

        assertTrue(JSONWriter.ofUTF8().isUTF8());
        assertFalse(JSONWriter.ofUTF8().isUTF16());
        assertFalse(JSONWriter.ofUTF16().isUTF8());
        assertTrue(JSONWriter.ofUTF16().isUTF16());
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
        jsonWriter.writeNull();

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
        jsonWriter.writeInt64((long[]) null);
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
    public void getBytes() {
        JSONWriter jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeChar('a');
        assertArrayEquals(new byte[]{'"', 'a', '"'}, jsonWriter.getBytes());
    }

    @Test
    public void isWriteMapTypeInfo() {
        JSONWriter jsonWriter = JSONWriter.of();
        assertFalse(jsonWriter.isWriteMapTypeInfo(null, null, 0));

        ConcurrentMap map = new ConcurrentHashMap();
        assertFalse(jsonWriter.isWriteMapTypeInfo(map, Map.class, 0));
        assertTrue(jsonWriter.isWriteMapTypeInfo(map, Map.class, WriteClassName.mask));

        jsonWriter.setRootObject(map);
        assertTrue(jsonWriter.isWriteMapTypeInfo(map, Map.class, WriteClassName.mask));
        assertFalse(jsonWriter.isWriteMapTypeInfo(map, Map.class, WriteClassName.mask | NotWriteRootClassName.mask));

        Map map1 = new HashMap();
        jsonWriter.setRootObject(map1);

        assertFalse(jsonWriter.isWriteMapTypeInfo(map1, Map.class, WriteClassName.mask | NotWriteHashMapArrayListClassName.mask));
        assertFalse(jsonWriter.isWriteMapTypeInfo(map1, Map.class, WriteClassName.mask | NotWriteRootClassName.mask | NotWriteHashMapArrayListClassName.mask));
    }

    @Test
    public void isWriteTypeInfo() {
        Map map = new HashMap();
        List list = new ArrayList();
        TreeMap treeMap = new TreeMap<>();

        Type mapType = new TypeReference<Map<String, Object>>() {
        }.getType();

        {
            JSONWriter jsonWriter = JSONWriter.of(WriteClassName);

            assertTrue(jsonWriter.isWriteTypeInfo(map, 0));
            assertFalse(jsonWriter.isWriteTypeInfo(map, NotWriteHashMapArrayListClassName.mask));
            assertFalse(jsonWriter.isWriteTypeInfo(list, NotWriteHashMapArrayListClassName.mask));
            assertTrue(jsonWriter.isWriteTypeInfo(treeMap, NotWriteHashMapArrayListClassName.mask));

            jsonWriter.config(NotWriteHashMapArrayListClassName);
            assertFalse(jsonWriter.isWriteTypeInfo(map));
            assertFalse(jsonWriter.isWriteTypeInfo(list));
            assertTrue(jsonWriter.isWriteTypeInfo(treeMap));

            assertFalse(jsonWriter.isWriteTypeInfo(null, (Type) Map.class, 0));
            assertFalse(jsonWriter.isWriteTypeInfo(map, (Type) Map.class, 0));
            assertTrue(jsonWriter.isWriteTypeInfo(treeMap, (Type) Map.class, 0));
            assertFalse(jsonWriter.isWriteTypeInfo(list, (Type) Map.class, 0));

            assertFalse(jsonWriter.isWriteTypeInfo(null, Map.class));
            assertFalse(jsonWriter.isWriteTypeInfo(map, Map.class));
            assertTrue(jsonWriter.isWriteTypeInfo(treeMap, Map.class));
            assertFalse(jsonWriter.isWriteTypeInfo(list, Map.class));

            assertFalse(jsonWriter.isWriteTypeInfo(null, mapType));
            assertFalse(jsonWriter.isWriteTypeInfo(map, mapType));
            assertTrue(jsonWriter.isWriteTypeInfo(treeMap, mapType));
            assertFalse(jsonWriter.isWriteTypeInfo(list, mapType));

            assertFalse(jsonWriter.isWriteTypeInfo(null, Map.class, 0));
            assertFalse(jsonWriter.isWriteTypeInfo(map, Map.class, 0));
            assertTrue(jsonWriter.isWriteTypeInfo(treeMap, Map.class, 0));
            assertFalse(jsonWriter.isWriteTypeInfo(list, Map.class, 0));
        }
    }

    @Test
    public void writeInt32() {
        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.writeInt32((int[]) null);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void writeInt321() {
        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.writeInt32((Integer) null);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void writeStringNull() {
        {
            JSONWriter jsonWriter = JSONWriter.of(NullAsDefaultValue);
            jsonWriter.writeStringNull();
            assertEquals("\"\"", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.of(NullAsDefaultValue, UseSingleQuotes);
            jsonWriter.writeStringNull();
            assertEquals("''", jsonWriter.toString());
        }
    }

    @Test
    public void writeDecimal() {
        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.writeDecimal(null, 0);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void writeString() {
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeString("\\".toCharArray());
            assertEquals("\"\\\\\"", jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeString("\"".toCharArray());
            assertEquals("\"\\\"\"", jsonWriter.toString());
        }
    }

    @Test
    public void writeList() {
        JSONArray array = JSONArray.of(1, 2, 3);
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.write(array);
            assertEquals("[1,2,3]", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.write(array);
            assertEquals("[1,2,3]", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.write(array);
            assertEquals("[1,2,3]", jsonWriter.toString());
        }
    }

    @Test
    public void writeList2() {
        JSONArray array = JSONArray.of(1L, 2L, 3L);
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.write(array);
            assertEquals("[1,2,3]", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.write(array);
            assertEquals("[1,2,3]", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.write(array);
            assertEquals("[1,2,3]", jsonWriter.toString());
        }
    }

    @Test
    public void writeList3() {
        JSONArray array = JSONArray.of(
                "abc",
                BigDecimal.ONE,
                2F,
                3D,
                true,
                false,
                JSONArray.of(),
                JSONObject.of(),
                new HashMap<>(),
                null
        );
        String expected = "[\"abc\",1,2.0,3.0,true,false,[],{},{},null]";
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.write(array);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.write(array);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.write(array);
            assertEquals(expected, jsonWriter.toString());
        }
    }

    @Test
    public void writeObject() {
        JSONObject object = JSONObject.of(
                "v0", true,
                "v1", BigDecimal.ONE,
                "v2", JSONObject.of(),
                "v3", JSONArray.of(),
                "v4", new HashMap<>());

        String expected = "{\"v0\":true,\"v1\":1,\"v2\":{},\"v3\":[],\"v4\":{}}";
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.write(object);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.write(object);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.write(object);
            assertEquals(expected, jsonWriter.toString());
        }
    }

    @Test
    public void writeAny() {
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeAny(1);
        byte[] bytes = jsonWriter.getBytes();
        assertEquals(1, JSONB.parse(bytes));
    }

    @Test
    public void pathEquals() {
        assertFalse(ROOT.equals(null));
        assertFalse(ROOT.equals(new Object()));
    }

    @Test
    public void pathToString() {
        assertEquals(
                "$.A",
                new JSONWriter.Path(ROOT, "A")
                        .toString()
        );
        assertEquals(
                "$.ABCDEABCDEABCDEABCDEABCDEABCDEABCDEABCDE",
                new JSONWriter.Path(ROOT, "ABCDEABCDEABCDEABCDEABCDEABCDEABCDEABCDE")
                        .toString()
        );

        assertEquals(
                "$.中",
                new JSONWriter.Path(ROOT, "中")
                        .toString()
        );
        assertEquals(
                "$.中中中中中中中中中中中中中中中中中中中中中中中中中",
                new JSONWriter.Path(ROOT, "中中中中中中中中中中中中中中中中中中中中中中中中中")
                        .toString()
        );

        assertEquals(
                "$.\uD83D\uDE0B",
                new JSONWriter.Path(ROOT, "\uD83D\uDE0B")
                        .toString()
        );
        assertEquals(
                "$.\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B",
                new JSONWriter.Path(
                        ROOT,
                        "\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B"
                )
                        .toString()
        );

        assertEquals(
                "$.Ɛ",
                new JSONWriter.Path(ROOT, "Ɛ")
                        .toString()
        );
        assertEquals(
                "$.ƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐ",
                new JSONWriter.Path(ROOT, "ƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐƐ")
                        .toString()
        );

        assertEquals(
                "$.?",
                new JSONWriter.Path(ROOT, "\uDC00")
                        .toString()
        );
    }

    @Test
    public void writeStringUTF8() {
        char[] chars = new char[1024 * 512 + 7];
        Arrays.fill(chars, 'A');

        String str = new String(chars);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(str);
            String json = jsonWriter.toString();
            assertEquals(chars.length + 2, json.length());
            for (int i = 0; i < chars.length; i++) {
                assertEquals(chars[i], +json.charAt(i + 1));
            }
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(chars, 0, chars.length, true);
            String json = jsonWriter.toString();
            assertEquals(chars.length + 2, json.length());
            for (int i = 0; i < chars.length; i++) {
                assertEquals(chars[i], +json.charAt(i + 1));
            }
        }
    }

    @Test
    public void writeStringUTF8_1() {
        char[] chars = new char[1024 * 32 + 7];
        Arrays.fill(chars, '中');

        String str = new String(chars);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(str);
            String json = jsonWriter.toString();
            assertEquals(chars.length + 2, json.length());
            for (int i = 0; i < chars.length; i++) {
                assertEquals(chars[i], +json.charAt(i + 1));
            }
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(chars, 0, chars.length, true);
            String json = jsonWriter.toString();
            assertEquals(chars.length + 2, json.length());
            for (int i = 0; i < chars.length; i++) {
                assertEquals(chars[i], +json.charAt(i + 1));
            }
        }
    }

    @Test
    public void writeStringUTF8_2() {
        char[] chars = new char[1024 * 32 + 7];
        Arrays.fill(chars, 'Ɛ');

        String str = new String(chars);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(str);
            String json = jsonWriter.toString();
            assertEquals(chars.length + 2, json.length());
            for (int i = 0; i < chars.length; i++) {
                assertEquals(chars[i], +json.charAt(i + 1));
            }
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(chars, 0, chars.length, true);
            String json = jsonWriter.toString();
            assertEquals(chars.length + 2, json.length());
            for (int i = 0; i < chars.length; i++) {
                assertEquals(chars[i], +json.charAt(i + 1));
            }
        }
    }

    @Test
    public void writeString_latin() {
        int[] ranges = {
                0x0000, 0x007F,
                0x0080, 0x00FF,
                0x0100, 0x017F,
                0x0180, 0x024F,
                0x0250, 0x02AF,
                0x02B0, 0x02FF,
                0x1D00, 0x1D7F,
                0x1D80, 0x1DBF,
                0x1E00, 0x1EFF,
                0x2070, 0x209F,
                0x2100, 0x214F,
                0x2150, 0x218F,
                0x2C60, 0x2C7F,
                0xA720, 0xA7FF,
                0xAB30, 0xAB6F,
                0xFF00, 0xFFEF,
                'A', 'Z',
                'a', 'z',
                '0', '9'
        };

        String[] strings = new String[1 + ranges.length / 2];

        {
            int size = 0, strIndex = 0;
            for (int i = 0; i < ranges.length; i += 2) {
                int start = ranges[i];
                int end = ranges[i + 1];

                size += (end - start) + 1;
            }
            char[] chars = new char[size];
            int off = 0;
            for (int i = 0; i < ranges.length; i += 2) {
                int start = ranges[i];
                int end = ranges[i + 1];

                char[] rangeChars = new char[end - start + 1];
                for (int j = start; j <= end; j++) {
                    chars[off++] = (char) j;
                    rangeChars[j - start] = (char) j;
                }
                strings[strIndex++] = new String(rangeChars);
            }

            strings[strIndex] = new String(chars);
        }

        for (String str : strings) {
            char[] chars = str.toCharArray();
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeString(str);
                String json = jsonWriter.toString();
                assertEquals(str, JSON.parse(json));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeString(chars, 0, chars.length, true);
                String json = jsonWriter.toString();
                assertEquals(str, JSON.parse(json));
            }

            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeString(str);
                String json = jsonWriter.toString();
                assertEquals(str, JSON.parse(json));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeString(chars, 0, chars.length, true);
                String json = jsonWriter.toString();
                assertEquals(str, JSON.parse(json));
            }

            {
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.writeString(str);
                byte[] jsonbBytes = jsonWriter.getBytes();
                Object parse = JSONB.parse(jsonbBytes);
                assertEquals(str, parse);
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.writeString(chars, 0, chars.length, true);
                byte[] jsonbBytes = jsonWriter.getBytes();
                assertEquals(str, JSONB.parse(jsonbBytes));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofJSONB();
                jsonWriter.writeString(chars);
                byte[] jsonbBytes = jsonWriter.getBytes();
                Object parse = JSONB.parse(jsonbBytes);
                assertEquals(str, parse);
            }
        }
    }

    @Test
    public void writeList1() {
        JSONArray array = JSONArray.of(1, 2, 3);
        String result = "[1,2,3]";
        String prettyResult = "[\n" +
                "\t1,\n" +
                "\t2,\n" +
                "\t3\n" +
                "]";

        assertEquals(result, array.toString());
        assertEquals(prettyResult, array.toString(PrettyFormat));
        assertEquals(prettyResult, JSONB.toJSONString(array.toJSONBBytes()));

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.write(array);
            assertEquals(result, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.write(array);
            assertEquals(result, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.write(array);
            assertEquals(prettyResult, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.write(array);
            assertEquals(prettyResult, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.write(array);
            byte[] jsonbBytes = jsonWriter.getBytes();
            assertEquals(prettyResult, JSONB.toJSONString(jsonbBytes));
        }
    }

    @Test
    public void writeObject1() {
        JSONObject object = JSONObject.of("id", 123);
        String result = "{\"id\":123}";
        String prettyResult = "{\n" +
                "\t\"id\":123\n" +
                "}";
        assertEquals(result, object.toString());
        assertEquals(prettyResult, object.toString(PrettyFormat));

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.write(object);
            assertEquals(result, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.write(object);
            assertEquals(result, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.write(object);
            assertEquals(prettyResult, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.write(object);
            assertEquals(prettyResult, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.write(object);
            byte[] jsonbBytes = jsonWriter.getBytes();
            assertEquals(prettyResult, JSONB.toJSONString(jsonbBytes));
        }
    }

    @Test
    public void writeObject2() {
        JSONObject object = JSONObject.of("id", 123, "name", "DataWorks");
        String result = "{\"id\":123,\"name\":\"DataWorks\"}";
        String prettyResult = "{\n" +
                "\t\"id\":123,\n" +
                "\t\"name\":\"DataWorks\"\n" +
                "}";
        assertEquals(result, object.toString());
        assertEquals(prettyResult, object.toString(PrettyFormat));

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.write(object);
            assertEquals(result, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.write(object);
            assertEquals(result, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.write(object);
            assertEquals(prettyResult, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.write(object);
            assertEquals(prettyResult, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.write(object);
            byte[] jsonbBytes = jsonWriter.getBytes();
            assertEquals(prettyResult, JSONB.toJSONString(jsonbBytes));
        }
    }

    @Test
    public void writeString1() {
        char[] chars = "01234567890".toCharArray();

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeString(null, 0, 0);
            assertEquals("null", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(null, 0, 0);
            assertEquals("null", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeString(chars, 5, 1);
            assertEquals("\"5\"", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.PrettyFormat);
            jsonWriter.writeString(chars, 5, 1);
            assertEquals("\"5\"", jsonWriter.toString());
        }
    }

    @Test
    public void writeString2() {
        List<String> list = Arrays.asList("a", "b");
        String expected = "[\"a\",\"b\"]";

        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeString(list);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(list);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeString(list);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeString(list);
            assertEquals(expected, jsonWriter.toString());
        }
    }

    @Test
    public void writeString3() {
        List<String> list = Arrays.asList("中国", "浙江");
        String expected = JSON.toJSONString(list);

        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeString(list);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(list);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeString(list);
            assertEquals(expected, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeString(list);
            assertEquals(expected, jsonWriter.toString());
        }
    }

    @Test
    public void test1() {
        A a = new A();
        a.id = 1001;
        A a1 = new A();
        a1.id = 1002;
        a.value = a1;

        byte[] jsonbBytes = JSONB.toBytes(a, WriteClassName);
        assertEquals("{\n" +
                "\t\"@type\":\"com.alibaba.fastjson2.JSONWriterTest$A#0\",\n" +
                "\t\"@value\":{\n" +
                "\t\t\"id\":1001,\n" +
                "\t\t\"value\":{\n" +
                "\t\t\t\"@type\":\"#0\",\n" +
                "\t\t\t\"@value\":{\n" +
                "\t\t\t\t\"id\":1002\n" +
                "\t\t\t}\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}", JSONB.toJSONString(jsonbBytes, true));

        A o = (A) JSONB.parseObject(jsonbBytes, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(a.id, o.id);
        assertEquals(((A) a.value).id, ((A) o.value).id);
    }

    public static class A {
        public Object value;
        public int id;
    }

    @Test
    public void test2() {
        A a = new A();
        a.id = 1001;
        A a1 = new A();
        a1.id = 1002;
        a.value = a1;

        byte[] jsonbBytes = JSONB.toBytes(a, WriteNameAsSymbol);
        assertEquals(
                "{\n" +
                        "\t\"id#0\":1001,\n" +
                        "\t\"value#1\":{\n" +
                        "\t\t\"#0\":1002\n" +
                        "\t}\n" +
                        "}",
                new JSONBDump(jsonbBytes, true).toString()
        );

        A o = JSONB.parseObject(jsonbBytes, A.class, JSONReader.Feature.SupportAutoType);
        assertEquals(a.id, o.id);
        assertEquals(a1.id, ((JSONObject) o.value).getIntValue("id"));
    }

    @Test
    public void test3() {
        A a = new A();
        a.id = 1001;
        A a1 = new A();
        a1.id = 1002;
        a.value = a1;

        byte[] jsonbBytes = JSONB.toBytes(a, WriteNameAsSymbol);
        assertEquals(
                "{\n" +
                        "\t\"id#0\":1001,\n" +
                        "\t\"value#1\":{\n" +
                        "\t\t\"#0\":1002\n" +
                        "\t}\n" +
                        "}",
                new JSONBDump(jsonbBytes, true).toString()
        );

        JSONObject root = JSONB.parseObject(jsonbBytes);
        assertEquals(a.id, root.getIntValue("id"));
        assertEquals(a1.id, root.getJSONObject("value").getIntValue("id"));
    }

    @Test
    public void test4() {
        Bean4 a = new Bean4();
        a.a1234567890id = 1001;
        Bean4 a1 = new Bean4();
        a1.a1234567890id = 1002;
        a.value = a1;

        byte[] jsonbBytes = JSONB.toBytes(a, WriteNameAsSymbol);
        assertEquals(
                "{\n" +
                        "\t\"a1234567890id#0\":1001,\n" +
                        "\t\"value#1\":{\n" +
                        "\t\t\"#0\":1002\n" +
                        "\t}\n" +
                        "}",
                new JSONBDump(jsonbBytes, true).toString()
        );

        JSONObject root = parseObject(jsonbBytes);
        assertEquals(a.a1234567890id, root.getIntValue("a1234567890id"));
        assertEquals(a1.a1234567890id, root.getJSONObject("value").getIntValue("a1234567890id"));
    }

    @Test
    public void test4_uf() {
        Bean4 a = new Bean4();
        a.a1234567890id = 1001;
        Bean4 a1 = new Bean4();
        a1.a1234567890id = 1002;
        a.value = a1;

        byte[] jsonbBytes = JSONB.toBytes(a, WriteNameAsSymbol);
        assertEquals(
                "{\n" +
                        "\t\"a1234567890id#0\":1001,\n" +
                        "\t\"value#1\":{\n" +
                        "\t\t\"#0\":1002\n" +
                        "\t}\n" +
                        "}",
                new JSONBDump(jsonbBytes, true).toString()
        );

        JSONObject root = parseObjectUF(jsonbBytes);
        assertEquals(a.a1234567890id, root.getIntValue("a1234567890id"));
        assertEquals(a1.a1234567890id, root.getJSONObject("value").getIntValue("a1234567890id"));
    }

    @Test
    public void test4_bean() {
        Bean4 a = new Bean4();
        a.a1234567890id = 1001;
        Bean4 a1 = new Bean4();
        a1.a1234567890id = 1002;
        a.value = a1;

        byte[] jsonbBytes = JSONB.toBytes(a, WriteNameAsSymbol);
        assertEquals(
                "{\n" +
                        "\t\"a1234567890id#0\":1001,\n" +
                        "\t\"value#1\":{\n" +
                        "\t\t\"#0\":1002\n" +
                        "\t}\n" +
                        "}",
                new JSONBDump(jsonbBytes, true).toString()
        );

        Bean4 o = parseObject(jsonbBytes, Bean4.class, JSONReader.Feature.SupportAutoType);
        assertEquals(a.a1234567890id, o.a1234567890id);
        assertEquals(a1.a1234567890id, o.value.a1234567890id);
    }

    @Test
    public void writeBigDecimalAsPlain() {
        BigDecimal[] decimals = new BigDecimal[]{
                new BigDecimal("1"),
                new BigDecimal("1.1"),
                new BigDecimal("-1.2"),
                new BigDecimal("1000"),
                new BigDecimal("-1000"),
                new BigDecimal("9007199254740991.12345")
        };
        for (BigDecimal decimal : decimals) {
            try (JSONWriter jsonWriter = JSONWriter.ofUTF8(WriteBigDecimalAsPlain)) {
                jsonWriter.writeDecimal(decimal);
                assertEquals(decimal.toPlainString(), jsonWriter.toString());
            }
            try (JSONWriter jsonWriter = JSONWriter.ofUTF16(WriteBigDecimalAsPlain)) {
                jsonWriter.writeDecimal(decimal);
                assertEquals(decimal.toPlainString(), jsonWriter.toString());
            }
        }
    }

    @Test
    public void test4_bean_uf() {
        Bean4 a = new Bean4();
        a.a1234567890id = 1001;
        Bean4 a1 = new Bean4();
        a1.a1234567890id = 1002;
        a.value = a1;

        byte[] jsonbBytes = JSONB.toBytes(a, WriteNameAsSymbol);
        assertEquals(
                "{\n" +
                        "\t\"a1234567890id#0\":1001,\n" +
                        "\t\"value#1\":{\n" +
                        "\t\t\"#0\":1002\n" +
                        "\t}\n" +
                        "}",
                new JSONBDump(jsonbBytes, true).toString()
        );

        Bean4 o = parseObjectUF(jsonbBytes, Bean4.class, JSONReader.Feature.SupportAutoType);
        assertEquals(a.a1234567890id, o.a1234567890id);
        assertEquals(a1.a1234567890id, o.value.a1234567890id);
    }

    public static class Bean4 {
        public int a1234567890id;
        public Bean4 value;
    }

    static JSONObject parseObject(byte[] jsonbBytes) {
        JSONReader.Context context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
        JSONReader reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);

        JSONObject object = (JSONObject) reader.readObject();
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static JSONObject parseObjectUF(byte[] jsonbBytes) {
        JSONReader.Context context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
        JSONReader reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);

        JSONObject object = (JSONObject) reader.readObject();
        if (reader.resolveTasks != null) {
            reader.handleResolveTasks(object);
        }
        return object;
    }

    static <T> T parseObject(byte[] jsonbBytes, Class<T> objectClass, JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
        context.config(features);
        JSONReader reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);

        return reader.read(objectClass);
    }

    static <T> T parseObjectUF(byte[] jsonbBytes, Class<T> objectClass, JSONReader.Feature... features) {
        JSONReader.Context context = new JSONReader.Context(JSONFactory.getDefaultObjectReaderProvider());
        context.config(features);

        JSONReader reader = new JSONReaderJSONB(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);

        return reader.read(objectClass);
    }

    @Test
    public void testError() {
        assertThrows(JSONException.class, () -> JSONWriter.of().writeSymbol(-1));
    }

    @Test
    public void ofJSONB() {
        JSONWriter.Context context = JSONFactory.createWriteContext();
        JSONWriter jsonWriter = JSONWriter.ofJSONB(context);
        assertSame(context, jsonWriter.getContext());
        jsonWriter.close();
    }

    @Test
    public void testPathHashCode() {
        assertTrue(ROOT.hashCode() != 0);
    }

    @Test
    public void testListInteger() {
        int[] values = new int[]{
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                Short.MIN_VALUE,
                Short.MAX_VALUE,
                Byte.MIN_VALUE,
                Byte.MAX_VALUE,
                -99,
                -49,
                -39,
                -29,
                -19,
                -9,
                -1,
                0,
                1,
                9,
                19,
                29,
                39,
                49,
                99
        };
        List<Integer> list = new ArrayList<>();
        for (int value : values) {
            list.add(value);
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeListInt32(list);
            String json = jsonWriter.toString();
            assertArrayEquals(values, JSON.parseObject(json, int[].class));
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeListInt32(list);
            String json = jsonWriter.toString();
            assertArrayEquals(values, JSON.parseObject(json, int[].class));
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeListInt32(list);
            byte[] jsonbBytes = jsonWriter.getBytes();
            assertArrayEquals(values, JSONB.parseObject(jsonbBytes, int[].class));
        }
    }

    @Test
    public void testListLong() {
        long[] values = new long[]{
                Long.MIN_VALUE,
                Long.MAX_VALUE,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                Short.MIN_VALUE,
                Short.MAX_VALUE,
                Byte.MIN_VALUE,
                Byte.MAX_VALUE,
                -99,
                -49,
                -39,
                -29,
                -19,
                -9,
                -1,
                0,
                1,
                9,
                19,
                29,
                39,
                49,
                99
        };
        List<Long> list = new ArrayList<>();
        for (long value : values) {
            list.add(value);
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeListInt64(list);
            String json = jsonWriter.toString();
            assertArrayEquals(values, JSON.parseObject(json, long[].class));
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeListInt64(list);
            String json = jsonWriter.toString();
            assertArrayEquals(values, JSON.parseObject(json, long[].class));
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeListInt64(list);
            byte[] jsonbBytes = jsonWriter.getBytes();
            assertArrayEquals(values, JSONB.parseObject(jsonbBytes, long[].class));
        }
    }

    @Test
    public void testWriteChar() {
        Map<Character, String> map = new LinkedHashMap<>();
        map.put('\n', "\"\\n\"");
        map.put('\r', "\"\\r\"");
        map.put('\b', "\"\\b\"");
        map.put('\f', "\"\\f\"");
        map.put('\t', "\"\\t\"");
        map.put('\0', "\"\\u0000\"");
        map.put('\7', "\"\\u0007\"");
        map.put((char) 11, "\"\\u000b\"");
        map.put((char) 16, "\"\\u0010\"");

        map.forEach((k, v) -> {
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeChar(k.charValue());
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeChar(k.charValue());
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeStringUTF16() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("中", "\"中\"");
        map.put("中\"", "\"中\\\"\"");
        map.put("ØÝ©", "\"ØÝ©\"");
        map.put("中\\\r\n\b\f\t", "\"中\\\\\\r\\n\\b\\f\\t\"");
        map.put("中\0", "\"中\\u0000\"");
        map.put("中\7", "\"中\\u0007\"");
        map.put("中" + ((char) 11), "\"中\\u000b\"");
        map.put("中" + ((char) 26), "\"中\\u001a\"");
        map.put("中" + ((char) 27), "\"中\\u001b\"");
        map.put("中" + ((char) 28), "\"中\\u001c\"");
        map.put("中" + ((char) 29), "\"中\\u001d\"");
        map.put("中\u0010", "\"中\\u0010\"");
        map.put("中\uD83D\uDC81\uD83D\uDC4C\uD83C\uDF8D\uD83D\uDE0D", "\"中\uD83D\uDC81\uD83D\uDC4C\uD83C\uDF8D\uD83D\uDE0D\"");

        map.forEach((k, v) -> {
            byte[] bytes = k.getBytes(StandardCharsets.UTF_16LE);
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeStringUTF16(bytes);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeStringUTF16(bytes);
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeStringUTF16_browserSecure() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("中<", "\"中\\u003C\"");
        map.put("中>", "\"中\\u003E\"");
        map.put("中(", "\"中\\u0028\"");
        map.put("中)", "\"中\\u0029\"");
        map.put("中\r)", "\"中\\r\\u0029\"");

        map.forEach((k, v) -> {
            byte[] bytes = k.getBytes(StandardCharsets.UTF_16LE);
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16(BrowserSecure);
                jsonWriter.writeStringUTF16(bytes);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8(BrowserSecure);
                jsonWriter.writeStringUTF16(bytes);
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeStringUTF16_EscapeNoneAscii() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("中\r中", "\"\\u4E2D\\r\\u4E2D\"");
        map.put("\uD83D\uDC81\uD83D\uDC4C\uD83C\uDF8D\uD83D\uDE0D", "\"\\uD83D\\uDC81\\uD83D\\uDC4C\\uD83C\\uDF8D\\uD83D\\uDE0D\"");
        map.put("中", "\"\\u4E2D\"");

        map.forEach((k, v) -> {
            byte[] bytes = k.getBytes(StandardCharsets.UTF_16LE);
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16(EscapeNoneAscii);
                jsonWriter.writeStringUTF16(bytes);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8(EscapeNoneAscii);
                jsonWriter.writeStringUTF16(bytes);
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeString_chars_EscapeNoneAscii_2() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("\uD83D\uDC81\uD83D\uDC4C\uD83C\uDF8D\uD83D\uDE0D", "\"\\uD83D\\uDC81\\uD83D\\uDC4C\\uD83C\\uDF8D\\uD83D\\uDE0D\"");
        map.put("中\r中", "\"\\u4E2D\\r\\u4E2D\"");
        map.put("中", "\"\\u4E2D\"");

        map.forEach((k, v) -> {
            char[] chars = k.toCharArray();
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16(EscapeNoneAscii);
                jsonWriter.writeString(chars);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16(EscapeNoneAscii);
                jsonWriter.writeString(chars, 0, chars.length);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16(EscapeNoneAscii);
                jsonWriter.writeString(chars, 0, chars.length, true);
                assertEquals(v, jsonWriter.toString());
            }

            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8(EscapeNoneAscii);
                jsonWriter.writeString(chars);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8(EscapeNoneAscii);
                jsonWriter.writeString(chars, 0, chars.length);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8(EscapeNoneAscii);
                jsonWriter.writeString(chars, 0, chars.length, true);
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeString_chars() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("\uD83D\uDC81\uD83D\uDC4C\uD83C\uDF8D\uD83D\uDE0D", "\"\uD83D\uDC81\uD83D\uDC4C\uD83C\uDF8D\uD83D\uDE0D\"");
        map.put("中\r中", "\"中\\r中\"");
        map.put("中", "\"中\"");

        map.forEach((k, v) -> {
            char[] chars = k.toCharArray();
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeString(chars);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeString(chars, 0, chars.length);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeString(chars, 0, chars.length, true);
                assertEquals(v, jsonWriter.toString());
            }

            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeString(chars);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeString(chars, 0, chars.length);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeString(chars, 0, chars.length, true);
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeStringLatin1() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("abc\rabc", "\"abc\\rabc\"");
        map.put("abc\"abc", "\"abc\\\"abc\"");
        map.put("abc\\abc", "\"abc\\\\abc\"");

        map.forEach((k, v) -> {
            byte[] bytes = k.getBytes(StandardCharsets.ISO_8859_1);
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeStringLatin1(bytes);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeStringLatin1(bytes);
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeStringLatin1_BrowserSecure() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("abc<abc", "\"abc\\u003Cabc\"");
        map.put("abc>abc", "\"abc\\u003Eabc\"");
        map.put("abc(abc", "\"abc\\u0028abc\"");
        map.put("abc)abc", "\"abc\\u0029abc\"");

        map.forEach((k, v) -> {
            byte[] bytes = k.getBytes(StandardCharsets.ISO_8859_1);
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16(BrowserSecure);
                jsonWriter.writeStringLatin1(bytes);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8(BrowserSecure);
                jsonWriter.writeStringLatin1(bytes);
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeChar() {
        Map<Character, String> map = new LinkedHashMap<>();
        map.put('c', "\"c\"");
        map.put('\u000b', "\"\\u000b\"");
        map.put('\u001b', "\"\\u001b\"");

        map.forEach((k, v) -> {
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeChar(k);
                assertEquals(v, jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeChar(k);
                assertEquals(v, jsonWriter.toString());
            }
        });
    }

    @Test
    public void test_writeStringArray() {
        String[] k = new String[]{"a", "b"};
        String v = "[\"a\",\"b\"]";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
    }

    @Test
    public void test_writeStringArray_null() {
        String[] k = null;
        String v = "null";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
    }

    @Test
    public void test_writeStringArray_1() {
        String[] k = new String[]{null, null};
        String v = "[\"\",\"\"]";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(NullAsDefaultValue);
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(NullAsDefaultValue);
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
    }

    @Test
    public void test_writeStringArray_3() {
        String[] k = new String[]{null, null};
        String v = "[\"\",\"\"]";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(WriteNullStringAsEmpty);
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(WriteNullStringAsEmpty);
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
    }

    @Test
    public void test_writeStringArray_2() {
        String[] k = new String[]{null, null};
        String v = "[null,null]";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(BrowserSecure);
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(BrowserSecure);
            jsonWriter.writeString(k);
            assertEquals(v, jsonWriter.toString());
        }
    }

    @Test
    public void test_flushTo() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("abc", "\"abc\"");

        for (Iterator<Map.Entry<String, String>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            String k = entry.getKey();
            String v = entry.getValue();
            byte[] bytes = k.getBytes(StandardCharsets.ISO_8859_1);
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.UTF_8);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.UTF_8));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.US_ASCII);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.US_ASCII));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.ISO_8859_1);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.ISO_8859_1));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.UTF_16);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.UTF_16));
            }

            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.UTF_8);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.UTF_8));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.US_ASCII);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.US_ASCII));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.ISO_8859_1);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.ISO_8859_1));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeStringLatin1(bytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.UTF_16);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.UTF_16));
            }
        }
    }

    @Test
    public void test_flushTo1() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("中国", "\"中国\"");

        for (Iterator<Map.Entry<String, String>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            String k = entry.getKey();
            String v = entry.getValue();
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeString(k);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeString(k);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.UTF_8);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.UTF_8));
            }

            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeString(k);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes));
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeString(k);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                jsonWriter.flushTo(out, StandardCharsets.UTF_8);
                byte[] outBytes = out.toByteArray();
                assertEquals(v, new String(outBytes, StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    public void test_writeOffsetTime() {
        LocalTime localTime = LocalTime.of(12, 13, 14);
        OffsetTime offsetTime = OffsetTime.of(localTime, ZoneOffset.ofHours(8));

        String expected = "\"12:13:14+08:00\"";

        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }
    }

    @Test
    public void test_writeOffsetTimeZ() {
        LocalTime localTime = LocalTime.of(12, 13, 14);
        OffsetTime offsetTime = OffsetTime.of(localTime, ZoneOffset.UTC);

        String expected = "\"12:13:14Z\"";

        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }
    }

    @Test
    public void test_writeOffsetTime_null() {
        OffsetTime offsetTime = null;

        String expected = "null";

        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.writeOffsetTime(offsetTime);
            assertEquals(expected, jsonWriter.toString());
        }
    }

    @Test
    public void test5() {
        JSONReader jsonReader = JSONReader.ofJSONB(new byte[0]);
        assertEquals(0, jsonReader.getRawInt());
        assertEquals(0, jsonReader.getRawLong());
        assertFalse(jsonReader.nextIfName4Match2());
        assertFalse(jsonReader.nextIfValue4Match2());
        assertFalse(jsonReader.nextIfName4Match3());
        assertFalse(jsonReader.nextIfValue4Match3());
        assertFalse(jsonReader.nextIfName4Match4((byte) 0));
        assertFalse(jsonReader.nextIfValue4Match4((byte) 0));
        assertFalse(jsonReader.nextIfName4Match5(0));
        assertFalse(jsonReader.nextIfValue4Match5((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match6(0));
        assertFalse(jsonReader.nextIfValue4Match6(0));
        assertFalse(jsonReader.nextIfName4Match7(0));
        assertFalse(jsonReader.nextIfValue4Match7(0));
        assertFalse(jsonReader.nextIfName4Match8((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfValue4Match8((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match9(0));
        assertFalse(jsonReader.nextIfValue4Match9(0, (byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match10(0));
        assertFalse(jsonReader.nextIfValue4Match10(0));
        assertFalse(jsonReader.nextIfName4Match11(0));
        assertFalse(jsonReader.nextIfValue4Match11(0));
        assertFalse(jsonReader.nextIfName4Match12(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match13(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match14(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match15(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match16(0, (byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match17(0, 0));
        assertFalse(jsonReader.nextIfName4Match18(0, 0));
        assertFalse(jsonReader.nextIfName4Match19(0, 0));
        assertFalse(jsonReader.nextIfName4Match20(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match21(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match22(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match23(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match24(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match25(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match26(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match27(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match28(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match29(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match30(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match31(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match32(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match33(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match34(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match35(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match36(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match37(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match38(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match39(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match40(0, 0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match41(0, 0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match42(0, 0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match43(0, 0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName8Match0());
        assertFalse(jsonReader.nextIfName8Match1());
        assertFalse(jsonReader.nextIfName8Match2());
    }

    @Test
    public void test5_chars() {
        JSONReader jsonReader = JSONReader.of("1234567890".toCharArray());
        assertFalse(jsonReader.nextIfName4Match2());
        assertFalse(jsonReader.nextIfValue4Match2());
        assertFalse(jsonReader.nextIfName4Match3());
        assertFalse(jsonReader.nextIfValue4Match3());
        assertFalse(jsonReader.nextIfName4Match4((byte) 0));
        assertFalse(jsonReader.nextIfValue4Match4((byte) 0));
        assertFalse(jsonReader.nextIfName4Match5(0));
        assertFalse(jsonReader.nextIfValue4Match5((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match6(0));
        assertFalse(jsonReader.nextIfValue4Match6(0));
        assertFalse(jsonReader.nextIfName4Match7(0));
        assertFalse(jsonReader.nextIfValue4Match7(0));
        assertFalse(jsonReader.nextIfName4Match8((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfValue4Match8((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match9(0));
        assertFalse(jsonReader.nextIfValue4Match9(0, (byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match10(0));
        assertFalse(jsonReader.nextIfValue4Match10(0));
        assertFalse(jsonReader.nextIfName4Match11(0));
        assertFalse(jsonReader.nextIfValue4Match11(0));
        assertFalse(jsonReader.nextIfName4Match12(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match13(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match14(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match15(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match16(0, (byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match17(0, 0));
        assertFalse(jsonReader.nextIfName4Match18(0, 0));
        assertFalse(jsonReader.nextIfName4Match19(0, 0));
        assertFalse(jsonReader.nextIfName4Match20(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match21(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match22(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match23(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match24(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match25(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match26(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match27(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match28(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match29(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match30(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match31(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match32(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match33(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match34(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match35(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match36(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match37(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match38(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match39(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match40(0, 0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match41(0, 0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match42(0, 0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match43(0, 0, 0, 0, 0));
        assertTrue(jsonReader.nextIfName8Match0());
        assertFalse(jsonReader.nextIfName8Match1());
        assertFalse(jsonReader.nextIfName8Match2());
    }

    @Test
    public void test5_utf8() {
        JSONReader jsonReader = JSONReader.of("1234567890".getBytes(StandardCharsets.UTF_8));
        assertFalse(jsonReader.nextIfName4Match2());
        assertFalse(jsonReader.nextIfValue4Match2());
        assertFalse(jsonReader.nextIfName4Match3());
        assertFalse(jsonReader.nextIfValue4Match3());
        assertFalse(jsonReader.nextIfName4Match4((byte) 0));
        assertFalse(jsonReader.nextIfValue4Match4((byte) 0));
        assertFalse(jsonReader.nextIfName4Match5(0));
        assertFalse(jsonReader.nextIfValue4Match5((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match6(0));
        assertFalse(jsonReader.nextIfValue4Match6(0));
        assertFalse(jsonReader.nextIfName4Match7(0));
        assertFalse(jsonReader.nextIfValue4Match7(0));
        assertFalse(jsonReader.nextIfName4Match8((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfValue4Match8((byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match9(0));
        assertFalse(jsonReader.nextIfValue4Match9(0, (byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match10(0));
        assertFalse(jsonReader.nextIfValue4Match10(0));
        assertFalse(jsonReader.nextIfName4Match11(0));
        assertFalse(jsonReader.nextIfValue4Match11(0));
        assertFalse(jsonReader.nextIfName4Match12(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match13(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match14(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match15(0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match16(0, (byte) 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match17(0, 0));
        assertFalse(jsonReader.nextIfName4Match18(0, 0));
        assertFalse(jsonReader.nextIfName4Match19(0, 0));
        assertFalse(jsonReader.nextIfName4Match20(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match21(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match22(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match23(0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match24(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match25(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match26(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match27(0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match28(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match29(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match30(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match31(0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match32(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match33(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match34(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match35(0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match36(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match37(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match38(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match39(0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match40(0, 0, 0, 0, 0, (byte) 0));
        assertFalse(jsonReader.nextIfName4Match41(0, 0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match42(0, 0, 0, 0, 0));
        assertFalse(jsonReader.nextIfName4Match43(0, 0, 0, 0, 0));
        assertTrue(jsonReader.nextIfName8Match0());
        assertFalse(jsonReader.nextIfName8Match1());
        assertFalse(jsonReader.nextIfName8Match2());
    }

    @Test
    public void test6() {
        {
            JSONReader jsonReader = JSONReader.of("1");
            assertEquals(1, jsonReader.readInt8Value());
        }
        {
            JSONReader jsonReader = JSONReader.of("1");
            assertEquals(1, jsonReader.readInt16Value());
        }
        {
            JSONReader jsonReader = JSONReader.of("{}");
            assertEquals(0, jsonReader.readJSONObject().size());
        }
    }

    @Test
    public void test7() {
        String[] strings = new String[] {
                "1",
                "1234567890",
                "1234567890123456789",
                "12345678901234567891234567890123456789",
                "1234567890123456789123456789012345678912345678901234567891234567890123456789",
                "1234567890123456789123456789012345678912345678901234567891234567890123456789",
                "1234567890123456789.1",
                "12345678901234567891234567890123456789.1",
                "12345678901234567891234567890123456789.123",
                "12345678901234567891234567890123456789.1234567890123456789"
        };
        for (String string : strings) {
            JSONReader jsonReader = JSONReader.of(string);
            jsonReader.readNumber0();
            assertEquals(new BigDecimal(string), jsonReader.getBigDecimal());
        }
        for (String string : strings) {
            JSONReader jsonReader = JSONReader.of(string, 0, string.length());
            jsonReader.readNumber0();
            assertEquals(new BigDecimal(string), jsonReader.getBigDecimal());
        }
    }

    @Test
    public void test9() {
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeInt32(1, new DecimalFormat("##"));
            assertEquals("\"1\"", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.of();
            jsonWriter.writeInt32(1, (DecimalFormat) null);
            assertEquals("1", jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofJSONB();
            jsonWriter.writeInt32(1, (DecimalFormat) null);
            assertEquals("1", JSONB.toJSONString(jsonWriter.getBytes()));
        }
    }

    @Test
    public void test10() {
        String[] strings = {"a", "b"};

        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeString(strings);
        byte[] bytes = jsonWriter.getBytes();

        JSONArray array = JSONB.parseArray(bytes);
        assertEquals(2, array.size());
        assertEquals(strings[0], array.get(0));
        assertEquals(strings[1], array.get(1));
    }

    @Test
    public void test11() {
        String str = "中国";
        byte[] utf16 = str.getBytes(StandardCharsets.UTF_16LE);
        JSONWriter jsonWriter = JSONWriter.ofJSONB();
        jsonWriter.writeStringUTF16(utf16);
        byte[] jsonb = jsonWriter.getBytes();
        Object parsed = JSONB.parse(jsonb);
        assertEquals(str, parsed);
    }

    @Test
    public void test12() {
        assertTrue(
                (((JSONWriterJSONB) JSONWriter.ofJSONB()))
                        .tryWriteStringUTF16("abc123456".getBytes(StandardCharsets.UTF_16LE)));
        assertTrue(
                (((JSONWriterJSONB) JSONWriter.ofJSONB()))
                        .tryWriteStringUTF16("中文12345678901234567890".getBytes(StandardCharsets.UTF_16LE)));
        assertTrue(
                (((JSONWriterJSONB) JSONWriter.ofJSONB()))
                        .tryWriteStringUTF16(
                                "1234567890123456789012345678901234567890中文".getBytes(StandardCharsets.UTF_16LE)));
    }

    @Test
    public void test13() {
        String str = "1234567890123456789012345678901234567890中文";
        byte[] bytes = JSONB.toBytes(str);
        JSONReader jsonReader = JSONReader.ofJSONB(bytes, 0, bytes.length, JSONFactory.createReadContext());
        assertEquals(str, jsonReader.readAny());
    }

    @Test
    public void test14() {
        String str = "1234567890123456789012345678901234567890";
        byte[] bytes = JSON.toJSONBytes(str);
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        JSONReader jsonReader = JSONReader.of(buf, StandardCharsets.UTF_8);
        assertEquals(str, jsonReader.readAny());
    }

    @Test
    public void writeInt16() {
        short[] values = new short[] {
                0,
                1,
                2,
                5,
                10,
                20,
                50,
                100,
                200,
                500,
                1000,
                2000,
                5000,
                10000,
                20000,
                -1,
                -2,
                -5,
                -10,
                -20,
                -50,
                -100,
                -200,
                -500,
                -1000,
                -2000,
                -5000,
                -10000,
                -20000,
                Short.MIN_VALUE,
                Short.MAX_VALUE
        };

        for (int i = 0; i < values.length; i++) {
            short value = values[i];

            {
                JSONWriter jsonWriter = JSONWriter.ofUTF8();
                jsonWriter.writeInt16(value);
                assertEquals(Short.toString(value), jsonWriter.toString());
            }
            {
                JSONWriter jsonWriter = JSONWriter.ofUTF16();
                jsonWriter.writeInt16(value);
                assertEquals(Short.toString(value), jsonWriter.toString());
            }
        }
    }

    @Test
    public void writeName2Raw() {
        String name = "id";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        long nameValue = JDKUtils.UNSAFE.getLong(Arrays.copyOf(bytes, 8), JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName2Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName2Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }
    }

    @Test
    public void writeName3Raw() {
        String name = "x23";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        long nameValue = JDKUtils.UNSAFE.getLong(Arrays.copyOf(bytes, 8), JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName3Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName3Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }
    }

    @Test
    public void writeName4Raw() {
        String name = "x234";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        long nameValue = JDKUtils.UNSAFE.getLong(Arrays.copyOf(bytes, 8), JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName4Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName4Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }
    }

    @Test
    public void writeName5Raw() {
        String name = "x2345";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        long nameValue = JDKUtils.UNSAFE.getLong(Arrays.copyOf(bytes, 8), JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName5Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName5Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }
    }

    @Test
    public void writeName6Raw() {
        String name = "x23456";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        long nameValue = JDKUtils.UNSAFE.getLong(Arrays.copyOf(bytes, 8), JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName6Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName6Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName6Raw(nameValue);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName6Raw(nameValue);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName7Raw() {
        String name = "x234567";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        long nameValue = JDKUtils.UNSAFE.getLong(Arrays.copyOf(bytes, 8), JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName7Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName7Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName7Raw(nameValue);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName7Raw(nameValue);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName8Raw() {
        String name = "x2345678";
        String expect = "{\"" + name + "\":";
        byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
        long nameValue = JDKUtils.UNSAFE.getLong(Arrays.copyOf(bytes, 8), JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName8Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName8Raw(nameValue);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName8Raw(nameValue);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName8Raw(nameValue);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName9Raw() {
        String name = "x23456789";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        byte[] bytes16 = Arrays.copyOf(bytes, 16);
        long nameValue = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        int nameValue1 = JDKUtils.UNSAFE.getInt(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + 8);

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName9Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName9Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName9Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName9Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName10Raw() {
        String name = "x234567890";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        byte[] bytes16 = Arrays.copyOf(bytes, 16);
        long nameValue = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        long nameValue1 = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + 8);

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName10Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName10Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName10Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName10Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName11Raw() {
        String name = "x2345678901";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        byte[] bytes16 = Arrays.copyOf(bytes, 16);
        long nameValue = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        long nameValue1 = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + 8);

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName11Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName11Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName11Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName11Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName12Raw() {
        String name = "x23456789012";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        byte[] bytes16 = Arrays.copyOf(bytes, 16);
        long nameValue = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        long nameValue1 = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + 8);

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName12Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName12Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName12Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName12Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName13Raw() {
        String name = "x234567890123";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        byte[] bytes16 = Arrays.copyOf(bytes, 16);
        long nameValue = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        long nameValue1 = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + 8);

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName13Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName13Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName13Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName13Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName14Raw() {
        String name = "x2345678901234";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        byte[] bytes16 = Arrays.copyOf(bytes, 16);
        long nameValue = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        long nameValue1 = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + 8);

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName14Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName14Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName14Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName14Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName15Raw() {
        String name = "x23456789012345";
        String expect = "{\"" + name + "\":";
        byte[] bytes = expect.substring(1).getBytes(StandardCharsets.UTF_8);
        byte[] bytes16 = Arrays.copyOf(bytes, 16);
        long nameValue = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        long nameValue1 = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + 8);

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName15Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName15Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName15Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName15Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }

    @Test
    public void writeName16Raw() {
        String name = "x234567890123456";
        String expect = "{\"" + name + "\":";
        byte[] bytes16 = name.getBytes(StandardCharsets.UTF_8);
        long nameValue = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET);
        long nameValue1 = JDKUtils.UNSAFE.getLong(bytes16, JDKUtils.ARRAY_BYTE_BASE_OFFSET + 8);

        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8();
            jsonWriter.startObject();
            jsonWriter.writeName16Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16();
            jsonWriter.startObject();
            jsonWriter.writeName16Raw(nameValue, nameValue1);
            assertEquals(expect, jsonWriter.toString());
        }

        String expect_pretty = "{\n\t\"" + name + "\":";
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF8(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName16Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
        {
            JSONWriter jsonWriter = JSONWriter.ofUTF16(PrettyFormat);
            jsonWriter.startObject();
            jsonWriter.writeName16Raw(nameValue, nameValue1);
            assertEquals(expect_pretty, jsonWriter.toString());
        }
    }
}
