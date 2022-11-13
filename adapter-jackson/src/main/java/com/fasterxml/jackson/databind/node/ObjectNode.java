package com.fasterxml.jackson.databind.node;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.adapter.jackson.TreeNodeUtils;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.BigInteger;

@JSONType(includes = "jsonObject")
public class ObjectNode
        extends ContainerNode {
    final JSONObject object;

    public ObjectNode() {
        object = new JSONObject();
    }

    public ObjectNode(JSONObject object) {
        this.object = object;
    }

    @JSONField(name = "jsonObject", value = true)
    public JSONObject getJSONObject() {
        return object;
    }

    public JsonNode path(String fieldName) {
        return get(fieldName);
    }

    public JsonNode get(String fieldName) {
        Object value = object.get(fieldName);
        return TreeNodeUtils.as(value);
    }

    @Override
    public String asText() {
        return object.toString();
    }

    public ObjectNode put(String fieldName, Short v) {
        object.put(fieldName, v);
        return this;
    }

    public ObjectNode put(String fieldName, Integer v) {
        object.put(fieldName, v);
        return this;
    }

    public ObjectNode put(String fieldName, Long v) {
        object.put(fieldName, v);
        return this;
    }

    public ObjectNode put(String fieldName, boolean v) {
        object.put(fieldName, v);
        return this;
    }

    public ObjectNode put(String fieldName, Float v) {
        object.put(fieldName, v);
        return this;
    }

    public ObjectNode put(String fieldName, Double v) {
        object.put(fieldName, v);
        return this;
    }

    public ObjectNode put(String fieldName, BigDecimal v) {
        object.put(fieldName, v);
        return this;
    }

    public ObjectNode put(String fieldName, BigInteger v) {
        object.put(fieldName, v);
        return this;
    }

    public ObjectNode put(String fieldName, String v) {
        object.put(fieldName, v);
        return this;
    }

    public ArrayNode putArray(String propertyName) {
        ArrayNode arrayNode = new ArrayNode();
        object.put(propertyName, arrayNode);
        return arrayNode;
    }

    public ObjectNode removeAll() {
        object.clear();
        return this;
    }

    @Override
    public int size() {
        return object.size();
    }

    public String toString() {
        return object.toString();
    }
}
