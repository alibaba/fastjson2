package com.alibaba.fastjson2.adapter.jackson.databind.ser.std;

import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;
import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonSerializer;
import com.alibaba.fastjson2.adapter.jackson.databind.SerializerProvider;

import java.io.IOException;

public abstract class StdSerializer<T>
        extends JsonSerializer<T> {
    protected final Class<T> handledType;

    protected StdSerializer(Class<T> t) {
        handledType = t;
    }

    protected StdSerializer(JavaType type) {
        handledType = (Class<T>) type.getRawClass();
    }

    @Override
    public abstract void serialize(T value, JsonGenerator gen, SerializerProvider provider)
            throws IOException;

    @Override
    public Class<T> handledType() {
        return handledType;
    }
}
