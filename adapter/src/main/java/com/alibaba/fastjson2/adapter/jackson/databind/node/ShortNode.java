package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;

public class ShortNode
        extends ValueNode {
    private short value;

    public ShortNode(@JSONField(name = "value", value = true) short value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public short getValue() {
        return value;
    }

    @Override
    public String asText() {
        return Short.toString(value);
    }

    public static ShortNode valueOf(short value) {
        return new ShortNode(value);
    }
}
