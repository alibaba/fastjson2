package com.fasterxml.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.databind.JsonNode;

public class DoubleNode
        extends JsonNode {
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
}
