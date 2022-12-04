package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;

import java.io.IOException;

public abstract class SerializerProvider
        extends DatabindContext {
    public final void defaultSerializeField(String fieldName, Object value, JsonGenerator gen)
            throws IOException {
        // TODO defaultSerializeField
        throw new JSONException("TODO");
    }

    public final void defaultSerializeNull(JsonGenerator gen) throws IOException {
        throw new JSONException("TODO");
    }

    public final void defaultSerializeValue(Object value, JsonGenerator gen) throws IOException {
        throw new JSONException("TODO");
    }

    public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass)
            throws IllegalArgumentException {
        throw new JSONException("TODO");
    }
}
