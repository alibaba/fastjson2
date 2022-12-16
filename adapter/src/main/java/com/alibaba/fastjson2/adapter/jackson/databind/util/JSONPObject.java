package com.alibaba.fastjson2.adapter.jackson.databind.util;

import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonSerializable;
import com.alibaba.fastjson2.adapter.jackson.databind.SerializerProvider;
import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

public class JSONPObject
        implements JsonSerializable {
    protected final String function;
    protected final Object value;

    public JSONPObject(String function, Object value) {
        this.function = function;
        this.value = value;
    }

    @Override
    public void serializeWithType(
            JsonGenerator gen,
            SerializerProvider provider,
            TypeSerializer typeSer
    ) throws IOException {
        serialize(gen, provider);
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeRaw(function);
        gen.writeRaw('(');
        gen.getJSONWriter().writeAny(value);
        gen.writeRaw(')');
    }

    public String getFunction() {
        return function;
    }

    public Object getValue() {
        return value;
    }
}
