package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;

import java.io.IOException;
import java.lang.reflect.Type;

public class DeserializationContext {
    final Type fieldType;
    final Object fieldName;
    final long features;

    public DeserializationContext(Type fieldType, Object fieldName, long features) {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.features = features;
    }

    public <T> T readValue(JsonParser p, Class<T> type) throws IOException {
        if (JsonNode.class.isAssignableFrom(type)) {
            return p.readValueAsTree();
        }
        return p.getJSONReader().read(type);
    }
}
