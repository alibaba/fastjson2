package com.alibaba.fastjson2.adapter.jackson.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonFactoryTest {
    @Test
    public void test() throws Exception {
        String carJson =
                "{ \"brand\" : \"Mercedes\", \"doors\" : 5 }";

        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(carJson);

        JsonToken[] tokens = new JsonToken[] {
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
}
