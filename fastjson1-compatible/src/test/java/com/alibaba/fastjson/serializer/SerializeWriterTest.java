package com.alibaba.fastjson.serializer;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class SerializeWriterTest {
    @Test
    public void writeNull() {
        SerializeWriter writer = new SerializeWriter();
        writer.writeNull();
        assertEquals("null", writer.toString());
    }

    @Test
    public void writeString() {
        SerializeWriter writer = new SerializeWriter();
        writer.writeString("abc");
        assertEquals("\"abc\"", writer.toString());
    }

    @Test
    public void write() {
        SerializeWriter writer = new SerializeWriter();
        writer.write("abc");
        assertEquals("abc", writer.toString());
    }

    @Test
    public void write1() {
        SerializeWriter writer = new SerializeWriter();
        writer.write('a');
        assertEquals("a", writer.toString());
    }

    @Test
    public void writeFieldName() throws Exception {
        SerializeWriter writer = new SerializeWriter();
        writer.writeFieldName("id");
        assertEquals(",\"id\"", writer.toString());
        assertEquals(",\"id\"", new String(writer.toBytes(StandardCharsets.UTF_8)));
        assertEquals(",\"id\"", new String(writer.toBytes("UTF8")));

        StringWriter out = new StringWriter();
        writer.writeTo(out);
        assertEquals(",\"id\"", out.toString());
    }

    @Test
    public void getBeforeFilters() {
        SerializeWriter writer = new SerializeWriter();
        assertTrue(writer.getBeforeFilters().isEmpty());
    }

    @Test
    public void testIsEnable() {
        SerializeWriter writer = new SerializeWriter(
                SerializerFeature.BeanToArray,
                SerializerFeature.WriteEnumUsingToString,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteEnumUsingName,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteClassName,
                SerializerFeature.NotWriteRootClassName,
                SerializerFeature.WriteNonStringKeyAsString,
                SerializerFeature.NotWriteDefaultValue,
                SerializerFeature.BrowserCompatible,
                SerializerFeature.IgnoreNonFieldGetter,
                SerializerFeature.WriteNonStringValueAsString,
                SerializerFeature.IgnoreErrorGetter,
                SerializerFeature.WriteBigDecimalAsPlain,
                SerializerFeature.WriteNullListAsEmpty
        );
        assertTrue(writer.isEnabled(SerializerFeature.BeanToArray));
        assertTrue(writer.isEnabled(SerializerFeature.WriteEnumUsingToString));
        assertTrue(writer.isEnabled(SerializerFeature.WriteMapNullValue));
        assertTrue(writer.isEnabled(SerializerFeature.WriteEnumUsingName));
        assertTrue(writer.isEnabled(SerializerFeature.WriteNullListAsEmpty));
        assertTrue(writer.isEnabled(SerializerFeature.WriteNullStringAsEmpty));
        assertTrue(writer.isEnabled(SerializerFeature.WriteNullNumberAsZero));
        assertTrue(writer.isEnabled(SerializerFeature.WriteNullBooleanAsFalse));
        assertTrue(writer.isEnabled(SerializerFeature.WriteClassName));
        assertTrue(writer.isEnabled(SerializerFeature.NotWriteRootClassName));
        assertTrue(writer.isEnabled(SerializerFeature.WriteNonStringKeyAsString));
        assertTrue(writer.isEnabled(SerializerFeature.NotWriteDefaultValue));
        assertTrue(writer.isEnabled(SerializerFeature.BrowserCompatible));
        assertTrue(writer.isEnabled(SerializerFeature.IgnoreNonFieldGetter));
        assertTrue(writer.isEnabled(SerializerFeature.WriteNonStringValueAsString));
        assertTrue(writer.isEnabled(SerializerFeature.IgnoreErrorGetter));
        assertTrue(writer.isEnabled(SerializerFeature.WriteBigDecimalAsPlain));
    }
}
