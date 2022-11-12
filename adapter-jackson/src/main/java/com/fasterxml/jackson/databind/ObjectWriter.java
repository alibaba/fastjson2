package com.fasterxml.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ser.FilterProvider;

import java.io.IOException;

public class ObjectWriter<T> {
    private SerializationConfig config;
    public ObjectWriter with(FilterProvider filterProvider) {
        // TODO
        throw new JSONException("TODO");
    }

    public ObjectWriter with(PrettyPrinter pp) {
        // TODO
        throw new JSONException("TODO");
    }

    public ObjectWriter forType(JavaType rootType) {
        // TODO
        throw new JSONException("TODO");
    }

    public ObjectWriter forType(Class<?> rootType) {
        // TODO
        throw new JSONException("TODO");
    }

    public SerializationConfig getConfig() {
        return config;
    }

    public void writeValue(JsonGenerator g, Object value) throws IOException {
        // TODO
        throw new JSONException("TODO");
    }
}
