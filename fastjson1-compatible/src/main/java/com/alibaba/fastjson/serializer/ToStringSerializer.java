package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.JSONWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public final class ToStringSerializer
        implements ObjectSerializer {
    public static final ToStringSerializer instance = new ToStringSerializer();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        String strVal = object.toString();
        jsonWriter.writeString(strVal);
    }

    @Override
    public void write(
            JSONSerializer serializer,
            Object object,
            Object fieldName,
            Type fieldType,
            int features) throws IOException {
        JSONWriter jsonWriter = serializer.out.raw;
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        String strVal = object.toString();
        jsonWriter.writeString(strVal);
    }
}
