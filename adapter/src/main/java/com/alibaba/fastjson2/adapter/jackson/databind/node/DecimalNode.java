package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
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

    @Override
    public int asInt(int defaultValue) {
        return value.intValue();
    }

    @Override
    public JsonParser.NumberType numberType() { return JsonParser.NumberType.BIG_DECIMAL; }

    @Override
    public long longValue() {
        return value.longValue();
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    public static DecimalNode valueOf(BigDecimal decimal) {
        return new DecimalNode(decimal);
    }

    public boolean isBigDecimal() {
        return true;
    }

    public double asDouble() {
        return value.doubleValue();
    }
}
