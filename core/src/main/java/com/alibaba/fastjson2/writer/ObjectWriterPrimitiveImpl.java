package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.function.Function;

public abstract class ObjectWriterPrimitiveImpl<T>
        implements ObjectWriter<T> {
    @Override
    public void writeArrayMappingJSONB(
            JSONWriter jsonWriter,
            Object object,
            Object fieldName,
            Type fieldType,
            long features
    ) {
        writeJSONB(jsonWriter, object, null, null, 0);
    }

    @Override
    public void writeArrayMapping(
            JSONWriter jsonWriter,
            Object object,
            Object fieldName,
            Type fieldType,
            long features
    ) {
        write(jsonWriter, object, null, null, 0);
    }

    public Function getFunction() {
        return null;
    }
}
