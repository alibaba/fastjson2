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
                SerializerFeature.WriteEnumUsingName
        );
        assertTrue(writer.isEnabled(SerializerFeature.BeanToArray));
        assertTrue(writer.isEnabled(SerializerFeature.WriteEnumUsingToString));
        assertTrue(writer.isEnabled(SerializerFeature.WriteEnumUsingName));
    }
}
