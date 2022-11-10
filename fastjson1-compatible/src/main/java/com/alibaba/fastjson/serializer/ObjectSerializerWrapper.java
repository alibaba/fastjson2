package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.writer.ObjectWriter;

import java.io.IOException;
import java.lang.reflect.Type;

public class ObjectSerializerWrapper
        implements ObjectSerializer {
    private final ObjectWriter raw;

    public ObjectSerializerWrapper(ObjectWriter raw) {
        this.raw = raw;
    }

    @Override
    public void write(
            JSONSerializer serializer,
            Object object,
            Object fieldName,
            Type fieldType,
            int features
    ) throws IOException {
        raw.write(serializer.raw, object, fieldName, fieldType, 0L);
    }
}
