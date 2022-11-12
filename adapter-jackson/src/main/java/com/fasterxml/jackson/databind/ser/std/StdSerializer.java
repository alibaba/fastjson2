package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public abstract class StdSerializer<T>
        extends JsonSerializer<T> {
    protected final Class<T> handledType;

    protected StdSerializer(Class<T> t) {
        handledType = t;
    }

    @Override
    public abstract void serialize(T value, JsonGenerator gen, SerializerProvider provider)
            throws IOException;
}
