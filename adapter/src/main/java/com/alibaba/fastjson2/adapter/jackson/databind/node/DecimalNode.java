package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;

import java.math.BigDecimal;

public class DecimalNode
        extends ValueNode {
    private BigDecimal value;

    public DecimalNode(@JSONField(name = "value", value = true) BigDecimal value) {
        this.value = value;
    }

    @JSONField(name = "value", value = true)
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public String asText() {
        return value.toPlainString();
    }
}
