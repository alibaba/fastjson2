package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonNode;

public abstract class ContainerNode
        extends JsonNode {
    public abstract int size();
}
