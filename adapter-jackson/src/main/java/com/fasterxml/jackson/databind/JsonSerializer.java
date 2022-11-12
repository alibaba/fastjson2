package com.fasterxml.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGeneratorWrapper;

import java.io.IOException;
import java.lang.reflect.Type;

public abstract class JsonSerializer<T>
        implements ObjectWriter {
    public abstract void serialize(T value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException;


    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        JsonGeneratorWrapper gen = new JsonGeneratorWrapper(jsonWriter);
        try {
            serialize((T) object, gen, null);
        } catch (IOException e) {
            throw new JSONException("write error");
        }
    }
}
