package com.alibaba.fastjson.serializer;

import org.junit.jupiter.api.Test;

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
    public void writeFieldName() {
        SerializeWriter writer = new SerializeWriter();
        writer.writeFieldName("id");
        assertEquals(",\"id\"", writer.toString());
    }

    @Test
    public void getBeforeFilters() {
        SerializeWriter writer = new SerializeWriter();
        assertTrue(writer.getBeforeFilters().isEmpty());
    }
}
