package com.alibaba.fastjson2.adapter.jackson.databind.node;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonNode;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

@JSONType(includes = "jsonArray")
public class ArrayNode
        extends ContainerNode<ArrayNode> {
    final JSONArray jsonArray;

    public ArrayNode(JsonNodeFactory nf) {
        this.jsonArray = new JSONArray();
    }

    public ArrayNode(JsonNodeFactory nf, Collection values) {
        this.jsonArray = new JSONArray(values);
    }

    public ArrayNode() {
        this.jsonArray = new JSONArray();
    }

    public ArrayNode(@JSONField(name = "jsonArray") JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @JSONField(name = "jsonArray", value = true)
    public JSONArray getJSONArray() {
        return jsonArray;
    }

    @Override
    public String asText() {
        return jsonArray.toString();
    }

    @Override
    public int size() {
        return jsonArray.size();
    }

    public ArrayNode add(Float v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(Double v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(BigDecimal v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(BigInteger v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(Long v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(Integer v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(Short v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(Byte v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(String v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(boolean v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(Boolean v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(JsonNode value) {
        jsonArray.add(value);
        return this;
    }

    public String toString() {
        return jsonArray.toString();
    }

    public boolean isArray() {
        return true;
    }

    @Override
    public Iterator<JsonNode> iterator() {
        return new JsonNodeIterator(jsonArray.iterator());
    }

    @Override
    public Iterator<JsonNode> elements() {
        return new JsonNodeIterator(this.iterator());
    }

    @Override
    public JsonNode get(int index) {
        if ((index >= 0) && (index < jsonArray.size())) {
            Object item = jsonArray.get(index);
            return TreeNodeUtils.as(item);
        }
        return null;
    }

    @Override
    public JsonNodeType getNodeType() {
        return JsonNodeType.ARRAY;
    }

    public ArrayNode removeAll() {
        jsonArray.clear();
        return this;
    }
}
