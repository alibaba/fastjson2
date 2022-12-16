package com.alibaba.fastjson2.adapter.jackson.core;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonGeneratorTest {
    @Test
    public void test() throws Exception {
        JsonFactory factory = new JsonFactory();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator generator = factory.createGenerator(out, JsonEncoding.UTF8);

        generator.writeStartObject();
        generator.writeStringField("brand", "Mercedes");
        generator.writeNumberField("doors", 5);
        generator.writeEndObject();

        generator.close();

        byte[] bytes = out.toByteArray();
        assertEquals("{\"brand\":\"Mercedes\",\"doors\":5}", new String(bytes, StandardCharsets.UTF_8));
    }

    @Test
    public void writeNull() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter out = new StringWriter();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeFieldName("value");
        gen.writeNull();
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"value\":null}", out.toString());
    }

    @Test
    public void writeNumberField() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter out = new StringWriter();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeNumberField("f0", 1);
        gen.writeNumberField("f1", 1L);
        gen.writeNumberField("f2", 1F);
        gen.writeNumberField("f3", 1D);
        gen.writeNumberField("f4", BigDecimal.ONE);
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"f0\":1,\"f1\":1,\"f2\":1.0,\"f3\":1.0,\"f4\":1}", out.toString());
    }

    @Test
    public void writeArray() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter out = new StringWriter();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeArrayFieldStart("value");
        gen.writeEndArray();
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"value\":[]}", out.toString());
    }

    @Test
    public void writeArray1() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter out = new StringWriter();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeFieldName("value");
        gen.writeStartArray();
        gen.writeEndArray();
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"value\":[]}", out.toString());
    }

    @Test
    public void writeNullField() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter out = new StringWriter();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeNullField("value");
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"value\":null}", out.toString());
    }

    @Test
    public void writeBooleanField() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter out = new StringWriter();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeBooleanField("value", true);
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"value\":true}", out.toString());
    }

    @Test
    public void writeObjectFieldStart() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter out = new StringWriter();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeObjectFieldStart("value");
        gen.writeEndObject();
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"value\":{}}", out.toString());
    }

    @Test
    public void writeObjectField() throws Exception {
        JsonFactory factory = new JsonFactory();
        StringWriter out = new StringWriter();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeObjectField("value", 1);
        gen.writeEndObject();
        gen.flush();
        assertEquals("{\"value\":1}", out.toString());
    }
}
