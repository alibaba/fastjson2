package com.alibaba.fastjson2.adapter.jackson.databind.node;

public class BooleanNode
        extends ValueNode {
    public static final BooleanNode TRUE = new BooleanNode(true);
    public static final BooleanNode FALSE = new BooleanNode(false);

    final boolean value;

    BooleanNode(boolean value) {
        this.value = value;
    }

    @Override
    public String asText() {
        return Boolean.toString(value);
    }

    public boolean booleanValue() {
        return value;
    }
}
