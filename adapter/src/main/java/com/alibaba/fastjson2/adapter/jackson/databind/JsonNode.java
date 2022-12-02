package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.core.TreeNode;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public abstract class JsonNode
        implements TreeNode, Iterable<JsonNode> {
    @Override
    public JsonNode get(String fieldName) { return null; }

    public abstract String asText();

    public int asInt() {
        return asInt(0);
    }

    public int asInt(int defaultValue) {
        return defaultValue;
    }

    @Override
    public String toString() {
        return asText();
    }

    public String toPrettyString() {
        return JSON.toJSONString(JSONWriter.Feature.PrettyFormat);
    }

    public JsonNode path(String fieldName) {
        return null;
    }

    public boolean isArray() {
        return false;
    }

    public Iterator<Map.Entry<String, JsonNode>> fields() {
        return Collections.emptyIterator();
    }

    @Override
    public Iterator<JsonNode> iterator() {
        throw new UnsupportedOperationException();
    }

    public Iterator<JsonNode> elements() {
        return Collections.emptyIterator();
    }

    public byte[] binaryValue() throws IOException {
        return null;
    }

    public String textValue() { return null; }

    public boolean isTextual() {
        return false;
    }

    public boolean has(int index) {
        return get(index) != null;
    }

    @Override
    public JsonNode get(int index) {
        return null;
    }

    public boolean has(String fieldName) {
        return get(fieldName) != null;
    }
}
