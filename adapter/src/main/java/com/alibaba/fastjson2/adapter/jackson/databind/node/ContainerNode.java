package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class ContainerNode<T extends ContainerNode<T>>
        extends JsonNode {
    public abstract int size();

    public final ValueNode numberNode(BigDecimal v) {
        return DecimalNode.valueOf(v);
    }

    public final ValueNode numberNode(BigInteger v) {
        return BigIntegerNode.valueOf(v);
    }

    public final ValueNode numberNode(long v) {
        return LongNode.valueOf(v);
    }

    public final ValueNode numberNode(int v) {
        return IntNode.valueOf(v);
    }

    public final ValueNode numberNode(float v) {
        return FloatNode.valueOf(v);
    }

    public final BooleanNode booleanNode(boolean v) {
        return BooleanNode.valueOf(v);
    }

    public final NullNode nullNode() {
        return NullNode.instance;
    }

    public final ValueNode numberNode(double v) {
        return DoubleNode.valueOf(v);
    }

    public final BinaryNode binaryNode(byte[] v) {
        return BinaryNode.valueOf(v);
    }

    public final TextNode textNode(String str) {
        return new TextNode(str);
    }
}
