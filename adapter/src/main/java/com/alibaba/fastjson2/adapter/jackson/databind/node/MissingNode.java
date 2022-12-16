package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonNode;

public class MissingNode
        extends JsonNode {
    private static final MissingNode instance = new MissingNode();

    public static MissingNode getInstance() {
        return instance;
    }

    @Override
    public String asText() {
        return null;
    }

    public boolean isMissingNode() {
        return true;
    }
}
