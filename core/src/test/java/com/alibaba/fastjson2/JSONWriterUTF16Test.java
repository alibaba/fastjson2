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
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteBooleanAsNumber);
        for (int i = 0; i < 8096; i++) {
            jsonWriter.writeNameRaw(new char[]{'a'});
        }
    }

    @Test
    public void test_writeNameRaw_1() {
        JSONWriter jsonWriter = JSONWriter.of(JSONWriter.Feature.WriteBooleanAsNumber);
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
}
