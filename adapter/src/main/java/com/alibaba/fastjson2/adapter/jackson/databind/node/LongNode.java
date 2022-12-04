package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;

import java.math.BigInteger;

public class LongNode
        extends ValueNode {
    private long value;

    public LongNode(@JSONField(name = "value", value = true) long value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public long getValue() {
        return value;
    }

    @Override
    public String asText() {
        return Long.toString(value);
    }

    @Override
    public int asInt(int defaultValue) {
        return (int) value;
    }

    public static LongNode valueOf(long value) {
        return new LongNode(value);
    }

    public BigInteger bigIntegerValue() {
        return BigInteger.valueOf(value);
    }
}
