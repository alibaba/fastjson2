package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.databind.util.RawValue;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonNodeFactory {
    public static final JsonNodeFactory instance = new JsonNodeFactory();

    public ValueNode rawValueNode(RawValue value) {
        return new POJONode(value);
    }

    public BinaryNode binaryNode(byte[] data) {
        return BinaryNode.valueOf(data);
    }

    public ValueNode numberNode(Byte value) {
        return (value == null) ? nullNode() : IntNode.valueOf(value.intValue());
    }

    public ValueNode numberNode(Short value) {
        return (value == null) ? nullNode() : ShortNode.valueOf(value);
    }

    public ValueNode numberNode(Integer value) {
        return (value == null) ? nullNode() : IntNode.valueOf(value.intValue());
    }

    public ValueNode numberNode(Long v) {
        if (v == null) {
            return nullNode();
        }
        return LongNode.valueOf(v.longValue());
    }

    public ValueNode numberNode(Float value) {
        return (value == null) ? nullNode() : FloatNode.valueOf(value.floatValue());
    }

    public ValueNode numberNode(Double value) {
        return (value == null) ? nullNode() : DoubleNode.valueOf(value.doubleValue());
    }

    public TextNode textNode(String text) {
        return TextNode.valueOf(text);
    }

    public NullNode nullNode() {
        return NullNode.getInstance();
    }

    public BooleanNode booleanNode(boolean v) {
        return v ? BooleanNode.TRUE : BooleanNode.FALSE;
    }

    public ValueNode numberNode(BigDecimal v) {
        return DecimalNode.valueOf(v);
    }

    public ValueNode numberNode(BigInteger v) {
        return BigIntegerNode.valueOf(v);
    }
}
