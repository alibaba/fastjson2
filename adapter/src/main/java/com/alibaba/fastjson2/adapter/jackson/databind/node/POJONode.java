package com.alibaba.fastjson2.adapter.jackson.databind.node;

public class POJONode
        extends ValueNode {
    protected final Object value;

    public POJONode(Object value) {
        this.value = value;
    }

    @Override
    public String asText() {
        return (value == null) ? "null" : value.toString();
    }
}
