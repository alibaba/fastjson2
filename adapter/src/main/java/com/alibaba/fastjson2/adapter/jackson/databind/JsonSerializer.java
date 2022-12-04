package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.adapter.jackson.core.JsonEncoding;
import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.TypeSerializer;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class JsonSerializer<T>
        implements ObjectWriter {
    public abstract void serialize(T value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException;

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        JsonGenerator gen = new JsonGenerator(jsonWriter, null, JsonEncoding.UTF8);
        try {
            serialize((T) object, gen, null);
        } catch (IOException e) {
            throw new JSONException("write error");
        }
    }

    public Class<T> handledType() {
        return null;
    }

    public abstract static class None
            extends JsonSerializer<Object> {
    }

    public void serializeWithType(
            T value,
            JsonGenerator gen,
            SerializerProvider serializers,
            TypeSerializer typeSer
    ) throws IOException {
        throw new JSONException("TODO");
    }
}
