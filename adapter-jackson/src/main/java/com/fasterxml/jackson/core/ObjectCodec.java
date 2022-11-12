package com.fasterxml.jackson.core;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public abstract class ObjectCodec {
    public abstract <T extends TreeNode> T readTree(JsonParser p) throws IOException;

    public TreeNode createObjectNode() {
        return new ObjectNode();
    }
}
