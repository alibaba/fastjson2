package com.fasterxml.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class ObjectReader {
    /**
     * Method for constructing a new reader instance that is configured
     * to data bind into specified type.
     * <p>
     * Note that the method does NOT change state of this reader, but
     * rather construct and returns a newly configured instance.
     *
     * @since 2.5
     */
    public ObjectReader forType(Class<?> valueType) {
        // TODO
        throw new JSONException("TODO");
    }

    /**
     * Method for constructing a new reader instance that is configured
     * to data bind into specified type.
     * <p>
     * Note that the method does NOT change state of this reader, but
     * rather construct and returns a newly configured instance.
     *
     * @since 2.5
     */
    public ObjectReader forType(TypeReference<?> valueTypeRef) {
        // TODO
        throw new JSONException("TODO");
    }

    public ObjectReader forType(JavaType valueType) {
        // TODO
        throw new JSONException("TODO");
    }

    public <T> T readValue(InputStream src) throws IOException {
        // TODO
        throw new JSONException("TODO");
    }

    public <T> T readValue(Reader src) throws IOException {
        // TODO
        throw new JSONException("TODO");
    }
}
