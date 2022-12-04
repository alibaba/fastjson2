package com.alibaba.fastjson2.adapter.jackson.databind.node;

public class NullNode
        extends ValueNode {
    public static final NullNode instance = new NullNode();

    @Override
    public String asText() {
        return null;
    }

    public static NullNode getInstance() {
        return instance;
    }

    @Override
    public boolean isTextual() {
        return true;
    }

    @Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.NULL;
    }
}
