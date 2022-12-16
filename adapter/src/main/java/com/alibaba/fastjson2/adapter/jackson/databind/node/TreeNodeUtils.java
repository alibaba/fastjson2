package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TreeNodeUtils {
    public static JsonNode as(Object value) {
        if (value == null) {
            return NullNode.instance;
        }

        if (value instanceof JsonNode) {
            return (JsonNode) value;
        }

        if (value instanceof String) {
            return new TextNode((String) value);
        }

        if (value instanceof Long) {
            return new LongNode((Long) value);
        }

        if (value instanceof Integer) {
            return new IntegerNode((Integer) value);
        }

        if (value instanceof BigDecimal) {
            return new DecimalNode((BigDecimal) value);
        }

        if (value instanceof BigInteger) {
            return new BigIntegerNode((BigInteger) value);
        }

        if (value instanceof Float) {
            return new FloatNode((Float) value);
        }

        if (value instanceof Double) {
            return new DoubleNode((Double) value);
        }

        if (value instanceof JSONObject) {
            return new ObjectNode((JSONObject) value);
        }

        if (value instanceof JSONArray) {
            return new ArrayNode((JSONArray) value);
        }

        if (value instanceof Boolean) {
            boolean booleanValue = ((Boolean) value).booleanValue();
            return booleanValue ? BooleanNode.TRUE : BooleanNode.FALSE;
        }

        if (value instanceof Short) {
            return new ShortNode((Short) value);
        }

        Object object = JSON.toJSON(value);
        if (object instanceof JSONObject) {
            return new ObjectNode((JSONObject) object);
        }

        if (object instanceof JSONArray) {
            return new ArrayNode((JSONArray) object);
        }

        throw new JSONException("TODO");
    }
}
