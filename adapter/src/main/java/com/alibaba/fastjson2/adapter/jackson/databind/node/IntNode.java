package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;

import java.math.BigInteger;

public class IntNode
        extends ValueNode {
    private int value;

    public IntNode(@JSONField(name = "value", value = true) int value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public int getValue() {
        return value;
    }

    public int asInt(int defaultValue) {
        return value;
    }

    @Override
    public String asText() {
        return Integer.toString(value);
    }

    public static IntNode valueOf(int i) {
        return new IntNode(i);
    }

    @Override
    public final JsonNodeType getNodeType() {
        return JsonNodeType.NUMBER;
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

    public BigInteger bigIntegerValue() {
        return BigInteger.valueOf(value);
    }
}
