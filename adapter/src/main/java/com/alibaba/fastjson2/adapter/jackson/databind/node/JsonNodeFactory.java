package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.databind.util.RawValue;

public class JsonNodeFactory {
    public ValueNode rawValueNode(RawValue value) {
        return new POJONode(value);
    }

    public NullNode nullNode() { return NullNode.getInstance(); }
}
