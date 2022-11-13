package com.fasterxml.jackson.databind.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.databind.JsonNode;

public class TextNode
        extends JsonNode {
    final String value;

    public TextNode(@JSONField(name = "value", value = true) String value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public String getValue() {
        return value;
    }

    @Override
    public String asText() {
        return value;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(value);
    }
}
