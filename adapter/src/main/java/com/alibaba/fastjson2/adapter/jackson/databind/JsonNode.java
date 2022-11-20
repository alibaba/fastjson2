package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.core.TreeNode;

import java.util.Iterator;

public abstract class JsonNode
        implements TreeNode, Iterable<JsonNode> {
    @Override
    public JsonNode get(String fieldName) { return null; }

    public abstract String asText();

    @Override
    public String toString() {
        return asText();
    }

    public JsonNode path(String fieldName) {
        return null;
    }

    public boolean isArray() {
        return false;
    }

    @Override
    public Iterator<JsonNode> iterator() {
        throw new UnsupportedOperationException();
    }
}
