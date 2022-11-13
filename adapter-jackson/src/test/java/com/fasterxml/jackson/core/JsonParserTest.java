package com.fasterxml.jackson.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonParserTest {
    @Test
    public void collectDefaults() {
        assertTrue(JsonParser.Feature.collectDefaults() > 0);
    }
}
