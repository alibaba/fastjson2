package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public interface ObjectSerializer
        extends ObjectWriter {
    @Override
    default void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        JSONSerializer jsonSerializer = new JSONSerializer(jsonWriter);
        try {
            write(jsonSerializer, object, fieldName, fieldType, 0);
        } catch (IOException e) {
            throw new JSONException("write error", e);
        }
    }

    void write(
            JSONSerializer serializer,
            Object object,
            Object fieldName,
            Type fieldType,
            int features) throws IOException;
}
