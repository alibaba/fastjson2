package com.fasterxml.jackson.core;

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
}
