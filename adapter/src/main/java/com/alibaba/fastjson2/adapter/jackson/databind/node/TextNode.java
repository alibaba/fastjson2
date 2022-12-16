package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonNode;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

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

    @Override
    public byte[] binaryValue() throws IOException {
        return Base64.getDecoder().decode(value);
    }

    public String textValue() {
        return value;
    }

    public final boolean isTextual() {
        return true;
    }

    @Override
    public int asInt(int defaultValue) {
        return Integer.parseInt(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TextNode jsonNodes = (TextNode) o;
        return Objects.equals(value, jsonNodes.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public static TextNode valueOf(String v) {
        return new TextNode(v);
    }

    @Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.STRING;
    }
}
