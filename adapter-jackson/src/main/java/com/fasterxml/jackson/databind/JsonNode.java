package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.TreeNode;

import java.util.Iterator;

public abstract class JsonNode
        implements TreeNode, Iterable<JsonNode> {

    @Override
    public JsonNode get(String fieldName) { return null; }

    public abstract String asText();

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
