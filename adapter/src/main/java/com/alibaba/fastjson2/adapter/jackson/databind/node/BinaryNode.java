package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonNode;

import java.io.IOException;

public class BinaryNode
        extends JsonNode {
    static final BinaryNode EMPTY_BINARY_NODE = new BinaryNode(new byte[0]);

    protected final byte[] data;

    public BinaryNode(byte[] data) {
        this.data = data;
    }

    @Override
    public String asText() {
        return null;
    }

    public byte[] binaryValue() throws IOException {
        return data;
    }

    public static BinaryNode valueOf(byte[] data) {
        if (data == null) {
            return null;
        }
        if (data.length == 0) {
            return EMPTY_BINARY_NODE;
        }
        return new BinaryNode(data);
    }
}
