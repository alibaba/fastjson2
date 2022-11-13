package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class ContainerNode
        extends JsonNode {
    public abstract int size();
}
