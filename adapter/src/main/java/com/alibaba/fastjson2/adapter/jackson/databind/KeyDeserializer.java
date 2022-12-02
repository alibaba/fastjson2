package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class KeyDeserializer
        implements com.alibaba.fastjson2.reader.ObjectReader {
    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        String str = jsonReader.readFieldName();
        DeserializationContext context = new DeserializationContext(fieldType, fieldName, features);
        try {
            return deserializeKey(str, context);
        } catch (IOException e) {
            throw new JSONException("deserializeKey error", e);
        }
    }

    public abstract Object deserializeKey(String key, DeserializationContext ctxt)
            throws IOException;

    public abstract static class None
            extends KeyDeserializer {
    }
}
