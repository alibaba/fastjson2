package com.alibaba.fastjson2.adapter.jackson.databind.node;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class POJONodeTest {
    @Test
    public void testNull() {
        assertEquals("null", new POJONode(null).asText());
    }
}
