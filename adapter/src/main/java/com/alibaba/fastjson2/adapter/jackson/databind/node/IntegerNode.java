package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;

public class IntegerNode
        extends ValueNode {
    private int value;

    public IntegerNode(@JSONField(name = "value", value = true) int value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public int getValue() {
        return value;
    }

    @Override
    public String asText() {
        return Integer.toString(value);
    }

    public int asInt(int defaultValue) {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public int intValue() {
        return value;
    }

    public double asDouble() {
        return value;
    }
}
