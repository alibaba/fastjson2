package com.fasterxml.jackson.databind.node;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

public class DecimalNode
        extends JsonNode {
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
