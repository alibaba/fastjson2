package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;

public class FloatNode
        extends ValueNode {
    private float value;

    public FloatNode(@JSONField(name = "value", value = true) float value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public float getValue() {
        return value;
    }

    @Override
    public String asText() {
        return Float.toString(value);
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

    public static FloatNode valueOf(float v) {
        return new FloatNode(v);
    }

    public boolean isFloat() {
        return true;
    }

    public double asDouble() {
        return value;
    }
}
