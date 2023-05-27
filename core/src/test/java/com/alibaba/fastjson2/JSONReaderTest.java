package com.alibaba.fastjson2;

import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JSONReaderTest {
    @Test
    public void test_null() {
        assertThrows(NullPointerException.class, () -> JSONReader.of((String) null));
    }

    @Test
    public void test_x() {
        String str = "{\"\\x69d\":\"\\x69d\" }";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        String fieldName = "id";
        String value = "id";
        long hash = Fnv.hashCode64(fieldName);
        long valueHash = Fnv.hashCode64(value);

        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
    }

    @Test
    public void test_comment() {
        String str = "// abc\n{// abc\n\"\\x69d\":\"\\x69d\" }";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        String fieldName = "id";
        String value = "id";
        long hash = Fnv.hashCode64(fieldName);
        long valueHash = Fnv.hashCode64(value);

        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
    }

    @Test
    public void test_x_2() {
        String str = "{\"\\x69d\":\"\\x69d®\" }";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        String fieldName = "id";
        String value = "id®";
        long hash = Fnv.hashCode64(fieldName);
        long valueHash = Fnv.hashCode64(value);

        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
    }

    @Test
    public void test_x_3() {
        String str = "{\"\\x69d\":\"\\x69d中国\" }";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        String fieldName = "id";
        String value = "id中国";
        long hash = Fnv.hashCode64(fieldName);
        long valueHash = Fnv.hashCode64(value);

        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
    }

    @Test
    public void test_x_4() {
        String str = "{\"\\x69d\":\"\\x69d\uD83D\uDE09\" }";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        String fieldName = "id";
        String value = "id\uD83D\uDE09";
        long hash = Fnv.hashCode64(fieldName);
        long valueHash = Fnv.hashCode64(value);

        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
    }

    @Test
    public void test_u() {
        char ch = '中';
        String str = "{\"\\u" + Integer.toHexString(ch) + "\":\"\\u" + Integer.toHexString(ch) + "\" }";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        String fieldName = new String(new char[]{ch});
        String value = fieldName;
        long hash = Fnv.hashCode64(fieldName);
        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfObjectStart());
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfObjectEnd());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
            assertTrue(reader.nextIfMatch('}'));
        }
    }

    @Test
    public void test_u_2() {
        char ch = '中';
        String str = "\\u" + Integer.toHexString(ch);
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        String fieldName = new String(new char[]{ch});
        String value = fieldName;
        long hash = Fnv.hashCode64(fieldName);
        {
            JSONReader reader = JSONReader.of(str);
            assertEquals(hash, reader.readFieldNameHashCodeUnquote());
            assertEquals(fieldName, reader.getFieldName());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertEquals(hash, reader.readFieldNameHashCodeUnquote());
            assertEquals(fieldName, reader.getFieldName());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            assertEquals(hash, reader.readFieldNameHashCodeUnquote());
            assertEquals(fieldName, reader.getFieldName());
        }
    }

    @Test
    public void test_next() {
        char ch = '中';
        String str = "{" + ch;
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONReader reader = JSONReader.of(str);
            reader.next();
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            reader.next();
            assertEquals(ch, reader.current());
        }
    }

    @Test
    public void test_next_2() {
        char ch = '®';
        String str = "{" + ch;
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        {
            JSONReader reader = JSONReader.of(str);
            reader.next();
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            reader.next();
            assertEquals(ch, reader.current());
        }
    }

    @Test
    public void test_readNumber_str() {
        String str = "\"\\x29\\u4e2dabcdef®中国\",1";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        {
            JSONException error = null;
            JSONReader reader = JSONReader.of(str);
            try {
                reader.readNumber();
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
            assertEquals('1', reader.current());
        }
        {
            JSONException error = null;
            JSONReader reader = JSONReader.of(strBytes);
            try {
                reader.readNumber();
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
            assertEquals('1', reader.current());
        }
    }

    @Test
    public void test_readNumber_str_2() {
        String str = "\"\\x29\\u4e2dabcdef\",1";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        {
            JSONReader reader = JSONReader.of(str);
            JSONException error = null;
            try {
                reader.readNumber();
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
            assertEquals('1', reader.current());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            JSONException error = null;
            try {
                reader.readNumber();
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
            assertEquals('1', reader.current());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            JSONException error = null;
            try {
                reader.readNumber();
            } catch (JSONException ex) {
                error = ex;
            }
            assertNotNull(error);
            assertEquals('1', reader.current());
        }
    }

    @Test
    public void test_readString_str_2() {
        String str = "\"\\x29\\u4e2dabcdef\",1";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        {
            JSONReader reader = JSONReader.of(str);
            assertEquals(")中abcdef", reader.readString());
            assertEquals('1', reader.current());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertEquals(")中abcdef", reader.readString());
            assertEquals('1', reader.current());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            assertEquals(")中abcdef", reader.readString());
            assertEquals('1', reader.current());
        }
    }

    @Test
    public void test_space() {
        final long SPACE
                = (1L << ' ')
                | (1L << '\n')
                | (1L << '\r')
                | (1L << '\t')
                | (1L << '\f')
                | (1L << '\b');

        for (int i = 0; i < 127; ++i) {
            char ch = (char) i;
            boolean space0 = ch <= ' ' && (ch == ' ' || ch == '\n' ||
                    ch == '\r' ||
                    ch == '\t' ||
                    ch == '\f' ||
                    ch == '\b');
            boolean space1 = ch <= ' ' && ((1L << ch) & SPACE) != 0;
            assertEquals(space0, space1);
        }
    }

    @Test
    public void testContext() {
        JSONReader jsonReader = JSONReader.of("{}");
        JSONReader.Context context = jsonReader.getContext();

        context.config(JSONReader.Feature.UseNativeObject, true);
        assertTrue(context.isEnabled(JSONReader.Feature.UseNativeObject));

        context.config(JSONReader.Feature.UseNativeObject, false);
        assertFalse(context.isEnabled(JSONReader.Feature.UseNativeObject));

        TimeZone defaultTimeZone = TimeZone.getDefault();
        context.setTimeZone(defaultTimeZone);
        assertSame(defaultTimeZone, context.getTimeZone());

        Locale china = Locale.CHINA;
        context.setLocale(china);
        assertSame(china, context.getLocale());

        ZoneId zoneId = ZoneId.systemDefault();
        context.setZoneId(zoneId);
        assertSame(zoneId, context.getZoneId());

        assertEquals(0, context.getFeatures());

        context.setDateFormatter(null);
        assertNull(context.getDateFormatter());

        context.setObjectSupplier(null);
        assertNull(context.getObjectSupplier());

        context.setArraySupplier(null);
        assertNull(context.getArraySupplier());
    }

    @Test
    public void getDecimal() {
        for (JSONReader jsonReader : TestUtils.createJSONReaders4("null")) {
            assertNull(
                    jsonReader.readBigDecimal()
            );
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("true")) {
            assertEquals(
                    BigDecimal.ONE,
                    jsonReader.readBigDecimal()
            );
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("{\"id\":123}")) {
            assertThrows(
                    Exception.class,
                    () -> jsonReader.readBigDecimal()
            );
        }

        for (JSONReader jsonReader : TestUtils.createJSONReaders4("[1]")) {
            assertThrows(
                    Exception.class,
                    () -> jsonReader.readBigDecimal()
            );
        }
    }

    @Test
    public void test_0() {
        char ch = 'a';
        String str = "{\"" + ch + "\":\"" + ch + "\" }";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);

        String fieldName = new String(new char[]{ch});
        String value = fieldName;
        long hash = Fnv.hashCode64(fieldName);
        {
            JSONReader reader = JSONReader.of(str);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
        }
    }

    @Test
    public void test_readValueHashCode() {
        for (int i = 1; i <= 20; i++) {
            char[] chars = new char[i];
            Arrays.fill(chars, 'A');
            String item = new String(chars);

            StringBuffer buf = new StringBuffer();
            buf.append("{\"");
            buf.append(item);
            buf.append("\":\"");
            buf.append(item);
            buf.append("\"}");

            long itemHash = Fnv.hashCode64(item);
            long itemHashL = Fnv.hashCode64LCase(item);
            String str = buf.toString();
            for (JSONReader jsonReader : TestUtils.createJSONReaders4(str)) {
                assertTrue(jsonReader.nextIfObjectStart());
                assertEquals(itemHash, jsonReader.readFieldNameHashCode());
                assertEquals(itemHashL, jsonReader.getNameHashCodeLCase());
                assertEquals(item, jsonReader.getFieldName());
                assertEquals(itemHash, jsonReader.readValueHashCode());
            }

            byte[] jsonbBytes = JSONObject.of(item, item).toJSONBBytes();
            JSONReader jsonReader = JSONReader.ofJSONB(jsonbBytes);
            assertTrue(jsonReader.nextIfObjectStart());
            assertEquals(itemHash, jsonReader.readFieldNameHashCode());
            assertEquals(itemHashL, jsonReader.getNameHashCodeLCase());
            assertEquals(item, jsonReader.getFieldName());
            assertEquals(itemHash, jsonReader.readValueHashCode());
        }
    }

    @Test
    public void readString_hasComma() {
        String str0 = "\"abc\",";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readString();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "\"abc\"";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readString();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "\"abc\",\"abc\"";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readString();
            assertTrue(jsonReader.hasComma());
            jsonReader.readString();
            assertFalse(jsonReader.hasComma());
        }

        String str3 = "\"abc\",\"abc\"}";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readString();
            assertTrue(jsonReader.hasComma());
            jsonReader.readString();
            assertFalse(jsonReader.hasComma());
        }

        String str4 = "\"abc\",\"abc\"]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str4)) {
            jsonReader.readString();
            assertTrue(jsonReader.hasComma());
            jsonReader.readString();
            assertFalse(jsonReader.hasComma());
        }
    }

    @Test
    public void skipValue_hasComma() {
        String str0 = "\"abc\",";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.skipValue();
            assertTrue(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str1 = "\"abc\"";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.skipValue();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str2 = "\"abc\",\"abc\"";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.skipValue();
            assertTrue(jsonReader.hasComma());
            jsonReader.skipValue();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "\"abc\",\"abc\"}";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.skipValue();
            assertTrue(jsonReader.hasComma());
            jsonReader.skipValue();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str4 = "\"abc\",\"abc\"]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str4)) {
            jsonReader.skipValue();
            assertTrue(jsonReader.hasComma());
            jsonReader.skipValue();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readInt64Value_hasComma() {
        String str0 = "123,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readInt64Value();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readInt64Value();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "123,123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readInt64Value();
            assertTrue(jsonReader.hasComma());
            jsonReader.readInt64Value();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "123,123]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readInt64Value();
            assertTrue(jsonReader.hasComma());
            jsonReader.readInt64Value();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readInt64_hasComma() {
        String str0 = "123,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readInt64();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readInt64();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "123,123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readInt64();
            assertTrue(jsonReader.hasComma());
            jsonReader.readInt64();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "123,123]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readInt64();
            assertTrue(jsonReader.hasComma());
            jsonReader.readInt64();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readInt32Value_hasComma() {
        String str0 = "123,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readInt32Value();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readInt32Value();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "123,123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readInt32Value();
            assertTrue(jsonReader.hasComma());
            jsonReader.readInt32Value();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "123,123]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readInt32Value();
            assertTrue(jsonReader.hasComma());
            jsonReader.readInt32Value();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readInt32_hasComma() {
        String str0 = "123,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readInt32();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readInt32();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "123,123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readInt32();
            assertTrue(jsonReader.hasComma());
            jsonReader.readInt32();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "123,123]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readInt32();
            assertTrue(jsonReader.hasComma());
            jsonReader.readInt32();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readDoubleValue_hasComma() {
        String str0 = "123,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readDoubleValue();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readDoubleValue();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "123,123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readDoubleValue();
            assertTrue(jsonReader.hasComma());
            jsonReader.readDoubleValue();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "123,123]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readDoubleValue();
            assertTrue(jsonReader.hasComma());
            jsonReader.readDoubleValue();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readDouble_hasComma() {
        String str0 = "123,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readDouble();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readDouble();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "123,123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readDouble();
            assertTrue(jsonReader.hasComma());
            jsonReader.readDouble();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "123,123]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readDouble();
            assertTrue(jsonReader.hasComma());
            jsonReader.readDouble();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readFloatValue_hasComma() {
        String str0 = "123,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readFloatValue();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readFloatValue();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "123,123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readFloatValue();
            assertTrue(jsonReader.hasComma());
            jsonReader.readFloatValue();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "123,123]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readFloatValue();
            assertTrue(jsonReader.hasComma());
            jsonReader.readFloatValue();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readFloat_hasComma() {
        String str0 = "123,";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str0)) {
            jsonReader.readFloat();
            assertTrue(jsonReader.hasComma());
        }

        String str1 = "123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str1)) {
            jsonReader.readFloat();
            assertFalse(jsonReader.hasComma());
        }

        String str2 = "123,123";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str2)) {
            jsonReader.readFloat();
            assertTrue(jsonReader.hasComma());
            jsonReader.readFloat();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }

        String str3 = "123,123]";
        for (JSONReader jsonReader : TestUtils.createJSONReaders4(str3)) {
            jsonReader.readFloat();
            assertTrue(jsonReader.hasComma());
            jsonReader.readFloat();
            assertFalse(jsonReader.hasComma(), jsonReader.getClass().getName());
        }
    }

    @Test
    public void readHex() {
        byte[] bytes = new byte[32];
        new Random().nextBytes(bytes);
        Bean bean = new Bean();
        bean.value = bytes;

        String str = JSON.toJSONString(bean);
        String str1 = JSON.toJSONString(bean, JSONWriter.Feature.OptimizedForAscii);
        assertEquals(str, str1);
        assertEquals(
                str,
                new String(
                        JSON.toJSONBytes(bean)
                )
        );
        String str2 = JSON.toJSONString(bean, JSONWriter.Feature.PrettyFormat);
        assertArrayEquals(
                JSON.parseObject(str).getBytes("value"),
                JSON.parseObject(str2).getBytes("value")
        );

        JSONWriter jsonWriter = JSONWriter.ofUTF8();
        jsonWriter.writeAny(bean);
        assertEquals(str, new String(jsonWriter.getBytes(StandardCharsets.UTF_8)));
        assertEquals(str, new String(jsonWriter.getBytes(StandardCharsets.US_ASCII)));
        assertEquals(str.length(), jsonWriter.size());

        Bean bean1 = JSON.parseObject(str, Bean.class);
        assertArrayEquals(bean.value, bean1.value);

        Bean bean2 = JSON.parseObject(str.getBytes(), Bean.class);
        assertArrayEquals(bean.value, bean2.value);
    }

    public static class Bean {
        @JSONField(format = "hex")
        public byte[] value;
    }

    @Test
    public void readLocalTime5() {
        String str = "\"12:34\"";
        JSONReader jsonReader = JSONReader.of(str.getBytes());
        LocalTime localTime = jsonReader.readLocalTime();
        assertEquals(12, localTime.getHour());
        assertEquals(34, localTime.getMinute());
        assertEquals(0, localTime.getSecond());
    }

    @Test
    public void nextIfInfinity() {
        assertFalse(JSONReader.ofJSONB(JSONB.toBytes(123)).nextIfInfinity());
        String str = "Infinity";
        char[] chars = str.toCharArray();
        byte[] bytes = str.getBytes();
        assertTrue(JSONReader.of(str).nextIfInfinity());
        assertTrue(JSONReader.of(chars).nextIfInfinity());
        assertTrue(JSONReader.of(bytes).nextIfInfinity());
        assertTrue(JSONReader.of(new StringReader(str)).nextIfInfinity());
        assertTrue(JSONReader.of(new StringReader(str), JSONFactory.createReadContext()).nextIfInfinity());
        assertTrue(JSONReader.of(JSONFactory.createReadContext(), str).nextIfInfinity());
    }

    @Test
    public void readMap() {
        String str = "{\"123\":\"456\",\"234\":\"567\"}";
        JSONReader jsonReader = JSONReader.of(JSONFactory.createReadContext(), str.toCharArray());
        Map map = new HashMap();
        jsonReader.read(map, Long.class, BigDecimal.class, 0L);
        assertEquals(new BigDecimal("456"), map.get(123L));
    }

    @Test
    public void readArray() {
        String str = "[\"123\",\"456\"]";
        JSONReader jsonReader = JSONReader.of(JSONFactory.createReadContext(), str.getBytes());
        Object[] array = jsonReader.readArray(new Type[]{Long.class, BigDecimal.class});
        assertEquals(123L, array[0]);
        assertEquals(new BigDecimal("456"), array[1]);
    }

    @Test
    public void readArray1() {
        String str = "[\"123\",\"456\", \"678\"]";
        char[] chars = str.toCharArray();
        JSONReader jsonReader = JSONReader.of(chars, 0, chars.length, JSONFactory.createReadContext());
        Object[] array = jsonReader.readArray(new Type[]{Long.class, BigDecimal.class, String.class});
        assertEquals(123L, array[0]);
        assertEquals(new BigDecimal("456"), array[1]);
        assertEquals("678", array[2]);
    }

    @Test
    public void autoTypeFilter() {
        JSONReader.AutoTypeBeforeHandler filter = JSONReader.autoTypeFilter(true, this.getClass());
        assertEquals(Boolean.class, filter.apply("Boolean", Boolean.class, 0L));
    }

    @Test
    public void context() {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        SymbolTable symbolTable = JSONB.symbolTable("id", "name");
        JSONReader.Context context = new JSONReader.Context(provider, symbolTable, JSONReader.Feature.SupportAutoType);
        assertSame(symbolTable, context.symbolTable);
        assertFalse(context.isFormatUnixTime());
        assertFalse(context.isFormatyyyyMMddhhmmss19());
        assertFalse(context.isFormatyyyyMMddhhmmssT19());
        assertFalse(context.isFormatyyyyMMdd8());
        assertFalse(context.isFormatMillis());
        assertFalse(context.isFormatISO8601());
        assertFalse(context.isFormatHasHour());

        context.setExtraProcessor(null);
        assertNull(context.getExtraProcessor());
    }

    @Test
    public void nextIfMatch() {
        char ch = '中';
        String str = "{" + ch;
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(ch, reader.current());
        }
    }

    @Test
    public void nextIfObjectStart() {
        char ch = '中';
        String str = "{" + ch;
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfObjectStart());
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfObjectStart());
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfObjectStart());
            assertEquals(ch, reader.current());
        }
    }

    @Test
    public void nextIfMatch_1() {
        char ch = '®';
        String str = "{" + ch;
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] latin1 = str.getBytes(StandardCharsets.ISO_8859_1);
        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(utf8);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(latin1, 0, latin1.length, StandardCharsets.ISO_8859_1);
            assertTrue(reader.nextIfMatch('{'));
            assertEquals(ch, reader.current());
        }
    }

    @Test
    public void nextIfObjectStart_1() {
        char ch = '®';
        String str = "{" + ch;
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] latin1 = str.getBytes(StandardCharsets.ISO_8859_1);
        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfObjectStart());
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfObjectStart());
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(utf8);
            assertTrue(reader.nextIfObjectStart());
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(latin1, 0, latin1.length, StandardCharsets.ISO_8859_1);
            assertTrue(reader.nextIfObjectStart());
            assertEquals(ch, reader.current());
        }
    }

    @Test
    public void nextIfMatch_end() {
        String str = "{";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfMatch('{'));
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfMatch('{'));
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfMatch('{'));
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.ISO_8859_1);
            assertTrue(reader.nextIfMatch('{'));
            assertTrue(reader.isEnd());
        }
    }

    @Test
    public void nextIfObjectStart_end() {
        String str = "{";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfObjectStart());
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfObjectStart());
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfObjectStart());
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.ISO_8859_1);
            assertTrue(reader.nextIfObjectStart());
            assertTrue(reader.isEnd());
        }
    }

    @Test
    public void nextIfObjectStart_comment_end() {
        String str = "// abc \n {";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        {
            JSONReader reader = JSONReader.of(str);
            assertTrue(reader.nextIfObjectStart());
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertTrue(reader.nextIfObjectStart());
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            assertTrue(reader.nextIfObjectStart());
            assertTrue(reader.isEnd());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.ISO_8859_1);
            assertTrue(reader.nextIfObjectStart());
            assertTrue(reader.isEnd());
        }
    }

    @Test
    public void next() {
        char ch = '®';
        String str = "{" + ch;
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] latin1 = str.getBytes(StandardCharsets.ISO_8859_1);
        {
            JSONReader reader = JSONReader.of(str);
            reader.next();
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            reader.next();
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(utf8);
            reader.next();
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(latin1, 0, latin1.length, StandardCharsets.ISO_8859_1);
            reader.next();
            assertEquals(ch, reader.current());
        }
    }

    @Test
    public void next_() {
        char ch = '®';
        String str = "®" + ch;
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] latin1 = str.getBytes(StandardCharsets.ISO_8859_1);
        {
            JSONReader reader = JSONReader.of(str);
            assertEquals(ch, reader.current());
            reader.next();
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(str.toCharArray());
            assertEquals(ch, reader.current());
            reader.next();
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(utf8);
            assertEquals(ch, reader.current());
            reader.next();
            assertEquals(ch, reader.current());
        }
        {
            JSONReader reader = JSONReader.of(latin1, 0, latin1.length, StandardCharsets.ISO_8859_1);
            assertEquals(ch, reader.current());
            reader.next();
            assertEquals(ch, reader.current());
        }
    }
}
