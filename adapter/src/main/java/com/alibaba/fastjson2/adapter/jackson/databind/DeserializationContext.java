package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.lang.reflect.Type;

public class DeserializationContext
        extends DatabindContext {
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

    public <T> T readValue(JsonParser p, JavaType javaType) throws IOException {
        Type type = javaType.getType();
        if (type instanceof Class && JsonNode.class.isAssignableFrom((Class) type)) {
            return p.readValueAsTree();
        }
        return p.getJSONReader().read(type);
    }

    @Override
    public Object getAttribute(Object key) {
        return null;
    }

    public final TypeFactory getTypeFactory() {
        throw new JSONException("TODO");
    }

    public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass)
            throws IllegalArgumentException {
        throw new JSONException("TODO");
    }
}
