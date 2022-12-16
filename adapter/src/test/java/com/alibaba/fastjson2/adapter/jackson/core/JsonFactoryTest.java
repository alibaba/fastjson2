package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.adapter.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonFactoryTest {
    @Test
    public void test() throws Exception {
        String carJson =
                "{ \"brand\" : \"Mercedes\", \"doors\" : 5 }";

        JsonFactory factory = new JsonFactory();
        factory.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        factory.disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
        JsonParser parser = factory.createParser(carJson);

        JsonToken[] tokens = new JsonToken[]{
                JsonToken.START_OBJECT,
                JsonToken.FIELD_NAME,
                JsonToken.VALUE_STRING,
                JsonToken.FIELD_NAME,
                JsonToken.VALUE_NUMBER_INT,
                JsonToken.END_OBJECT
        };

        int i = 0;
        while (!parser.isClosed()) {
            JsonToken jsonToken = parser.nextToken();
            assertEquals(tokens[i++], jsonToken);
        }
    }

    @Test
    public void test1() throws Exception {
        JsonFactory factory = new JsonFactory();
        factory.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        factory.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, true);
        factory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        factory.configure(JsonParser.Feature.ALLOW_COMMENTS, false);
    }

    @Test
    public void test2() throws Exception {
        JsonFactory factory = new JsonFactory();
        String str = "{\"id\":123}";
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        File tempFile = File.createTempFile("tmp", "json");

        ObjectMapper mapper = new ObjectMapper();
        JsonGenerator generator = factory.createGenerator(tempFile, JsonEncoding.UTF8);
        generator.writeStartObject();
        generator.writeNumberField("id", 123);
        generator.writeEndObject();
        generator.flush();
        generator.close();

        assertEquals(123,
                mapper.readValue(
                        factory.createParser(new StringReader(str)), Bean.class
                ).id
        );
        assertEquals(123,
                mapper.readValue(
                        factory.createParser(
                                new ByteArrayInputStream(strBytes)
                        ), Bean.class
                ).id
        );
        assertEquals(123,
                mapper.readValue(tempFile, Bean.class).id
        );
    }

    @Test
    public void test3() throws Exception {
        JsonFactory factory = new JsonFactory();
        String str = "{\"id\":123}";

        StringWriter writer = new StringWriter();
        JsonGenerator gen = factory.createGenerator(writer);
        gen.writeStartObject();
        gen.writeNumberField("id", 123);
        gen.writeEndObject();
        gen.flush();
        gen.close();
        assertEquals(str, writer.toString());
    }

    @Test
    public void test4() throws Exception {
        JsonFactory factory = new JsonFactory();
        String str = "{\"id\":123}";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator gen = factory.createGenerator(out);
        gen.writeStartObject();
        gen.writeNumberField("id", 123);
        gen.writeEndObject();
        gen.flush();
        gen.close();
        assertEquals(str, new String(out.toByteArray()));
    }

    public static class Bean {
        public int id;
    }
}
