package com.fasterxml.jackson.databind.node;

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
}
