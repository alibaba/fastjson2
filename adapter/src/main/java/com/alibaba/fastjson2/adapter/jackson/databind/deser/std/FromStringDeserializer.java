package com.alibaba.fastjson2.adapter.jackson.databind.deser.std;

import com.alibaba.fastjson2.adapter.jackson.core.JacksonException;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.databind.DeserializationContext;

import java.io.IOException;

public abstract class FromStringDeserializer<T>
        extends StdScalarDeserializer<T> {
    protected FromStringDeserializer(Class vc) {
        super(vc);
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JacksonException {
        String str = p.getJSONReader().readString();
        return _deserialize(str, ctx);
    }

    protected abstract T _deserialize(String value, DeserializationContext context);
}
