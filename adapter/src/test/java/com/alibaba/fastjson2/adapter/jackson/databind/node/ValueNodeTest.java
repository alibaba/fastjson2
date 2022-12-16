package com.alibaba.fastjson2.adapter.jackson.databind.node;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValueNodeTest {
    @Test
    public void testInt() throws Exception {
        IntNode node = IntNode.valueOf(123);
        assertEquals(123, node.asInt());
        assertEquals("123", node.asText());
        assertEquals(123, node.getValue());
        assertEquals(JsonNodeType.NUMBER, node.getNodeType());
        assertNull(node.binaryValue());
        assertNull(node.textValue());
        assertFalse(node.isTextual());
        assertFalse(node.has(1));
        assertFalse(node.has("1"));
    }

    @Test
    public void testBinary() throws Exception {
        byte[] bytes = new byte[]{1};
        BinaryNode node = BinaryNode.valueOf(bytes);
        assertSame(bytes, node.binaryValue());
        assertEquals(0, node.asInt());
    }
}
