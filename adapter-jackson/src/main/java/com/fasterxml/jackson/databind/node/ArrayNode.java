package com.fasterxml.jackson.databind.node;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.adapter.jackson.TreeNodeUtils;
import com.alibaba.fastjson2.annotation.JSONField;
import com.alibaba.fastjson2.annotation.JSONType;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.Iterator;

@JSONType(includes = "jsonArray")
public class ArrayNode
        extends JsonNode {
    final JSONArray jsonArray;

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

    public ArrayNode add(BigDecimal v) {
        jsonArray.add(v);
        return this;
    }

    public ArrayNode add(String v) {
        jsonArray.add(v);
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
        return new ArrayNodeIterator(jsonArray.iterator());
    }

    static class ArrayNodeIterator
            implements Iterator<JsonNode> {
        final Iterator iterator;

        public ArrayNodeIterator(Iterator iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public JsonNode next() {
            Object obj = iterator.next();
            return TreeNodeUtils.as(obj);
        }
    }
}
