package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;

import java.math.BigInteger;

public class BigIntegerNode
        extends ValueNode {
    private BigInteger value;

    public BigIntegerNode(@JSONField(name = "value", value = true) BigInteger value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public BigInteger getValue() {
        return value;
    }

    @Override
    public String asText() {
        return value.toString();
    }

    @Override
    public int asInt(int defaultValue) {
        return value.intValue();
    }

    @Override
    public final JsonNodeType getNodeType() {
        return JsonNodeType.NUMBER;
    }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    public static BigIntegerNode valueOf(BigInteger v) {
        return new BigIntegerNode(v);
    }

    public boolean isBigInteger() {
        return true;
    }

    public double asDouble() {
        return value.doubleValue();
    }

    public BigInteger bigIntegerValue() {
        return value;
    }
}
