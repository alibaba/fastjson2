package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.adapter.jackson.core.JacksonException;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class JsonDeserializer<T>
        implements ObjectReader {
    public abstract T deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException, JacksonException;

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        JsonParser p = new JsonParserWrapper(jsonReader);
        DeserializationContext ctx = new DeserializationContext(fieldType, fieldName, features);
        T object = null;
        try {
            object = deserialize(p, ctx);
        } catch (IOException e) {
            throw new JSONException("deserialize error", e);
        }
        return object;
    }
}
