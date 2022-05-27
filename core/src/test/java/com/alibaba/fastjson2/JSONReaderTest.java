package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.Fnv;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

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
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
        }
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
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
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
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
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
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
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
        }
        {
            JSONReader reader = JSONReader.of(strBytes);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(valueHash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
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
        {
            JSONReader reader = JSONReader.of(strBytes, 0, strBytes.length, StandardCharsets.US_ASCII);
            reader.nextIfMatch('{');
            assertEquals(hash, reader.readFieldNameHashCode());
            assertEquals(hash, reader.getNameHashCodeLCase());
            assertEquals(fieldName, reader.getFieldName());
            assertEquals(hash, reader.readValueHashCode());
            assertEquals(value, reader.getString());
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

        context.setDateFormat(null);
        assertNull(context.getDateFormat());

        context.setObjectSupplier(null);
        assertNull(context.getObjectSupplier());
    }
}
