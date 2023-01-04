package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.JSONBDump;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        jsonWriter.writeInt32(null);
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
        JSONWriter jsonWriter = JSONWriter.of();
        jsonWriter.write(JSONArray.of(1, 2, 3));
        assertEquals("[1,2,3]", jsonWriter.toString());
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
                assertEquals(str, JSONB.parse(jsonbBytes));
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
                assertEquals(str, JSONB.parse(jsonbBytes));
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
            JSONWriter jsonWriter = new JSONWriterPretty(JSONWriter.ofUTF16());
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
                "}", new JSONBDump(jsonbBytes, true).toString());

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
        JSONReader reader = new JSONReaderJSONBUF(
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

        JSONReader reader = new JSONReaderJSONBUF(
                context,
                jsonbBytes,
                0,
                jsonbBytes.length);

        return reader.read(objectClass);
    }
}
