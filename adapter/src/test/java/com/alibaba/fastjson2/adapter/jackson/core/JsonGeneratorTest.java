package com.alibaba.fastjson2.adapter.jackson.core;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
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
}
