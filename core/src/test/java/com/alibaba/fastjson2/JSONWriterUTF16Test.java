package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONWriterUTF16Test {
    @Test
    public void test_write() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeBool(true);
        }
    }

    @Test
    public void test_writeNameRaw() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeNameRaw(new char[]{'a'});
        }
    }

    @Test
    public void test_writeNameRaw_1() {
        JSONWriter jsonWriter = JSONWriter.ofUTF16(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeNameRaw(new char[]{'a'}, 0, 1);
        }
    }

    @Test
    public void testWriterString0() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeString((String) null);
        assertEquals("null", jsonWriter.toString());
    }

    @Test
    public void testWriterString1() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.NullAsDefaultValue));
        jsonWriter.writeString((String) null);
        assertEquals("\"\"", jsonWriter.toString());
    }

    @Test
    public void testWriterString2() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.NullAsDefaultValue));
        jsonWriter.writeString("abcdefghijk1234567890abcdefghijk1234567890");
        assertEquals("\"abcdefghijk1234567890abcdefghijk1234567890\"", jsonWriter.toString());
    }

    @Test
    public void testWriterString3() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.NullAsDefaultValue));
        jsonWriter.writeString("abcdefghijk1234567890\\\"abcdefghijk1234567890");
        assertEquals("\"abcdefghijk1234567890\\\\\\\"abcdefghijk1234567890\"", jsonWriter.toString());
    }

    @Test
    public void writeDouble() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        jsonWriter.writeDoubleArray(1, 2);
        assertEquals("[1.0,2.0]", jsonWriter.toString());
    }

    @Test
    public void writeColon() {
        final int COUNT = 100_000;
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        final int COUNT = 100_000;
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        final int COUNT = 100_000;
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeRaw(new char[]{','});
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeNameRaw(new char[0]);
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
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
        for (int i = 0; i < COUNT; i++) {
            jsonWriter.writeNameRaw(new char[0], 0, 0);
        }
        String string = jsonWriter.toString();
        assertEquals(COUNT, string.length());
        for (int i = 0; i < string.length(); i++) {
            assertEquals(',', string.charAt(i));
        }
    }

    @Test
    public void writeInt64() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext(JSONWriter.Feature.BrowserCompatible));
        jsonWriter.startArray();
        jsonWriter.writeInt64(9007199254740992L);
        jsonWriter.writeComma();
        jsonWriter.writeInt64(-9007199254740992L);
        jsonWriter.endArray();
        assertEquals("[\"9007199254740992\",\"-9007199254740992\"]", jsonWriter.toString());
    }

    @Test
    public void writeInt64_1() {
        JSONWriterUTF16 jsonWriter = new JSONWriterUTF16(JSONFactory.createWriteContext());
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
}
