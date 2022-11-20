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
}
