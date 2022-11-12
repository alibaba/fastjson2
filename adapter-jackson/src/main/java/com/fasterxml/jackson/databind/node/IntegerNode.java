package com.fasterxml.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.databind.JsonNode;

public class IntegerNode
        extends JsonNode {
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
}
