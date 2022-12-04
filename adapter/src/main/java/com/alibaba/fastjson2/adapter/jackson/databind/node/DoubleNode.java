package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;

public class DoubleNode
        extends ValueNode {
    private double value;

    public DoubleNode(@JSONField(name = "value", value = true) double value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public double getValue() {
        return value;
    }

    @Override
    public String asText() {
        return Double.toString(value);
    }

    @Override
    public int asInt(int defaultValue) {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    public static DoubleNode valueOf(double value) {
        return new DoubleNode(value);
    }

    public boolean isDouble() {
        return true;
    }

    public double asDouble() {
        return value;
    }
}
