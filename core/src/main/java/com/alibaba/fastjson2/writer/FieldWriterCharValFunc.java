package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToCharFunction;

import java.lang.reflect.Method;

final class FieldWriterCharValFunc
        extends FieldWriter {
    final ToCharFunction function;

    FieldWriterCharValFunc(String fieldName, int ordinal, long features, String format, String label, Method method, ToCharFunction function) {
        super(fieldName, ordinal, features, format, label, char.class, char.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.applyAsChar(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        char value = function.applyAsChar(object);
        jsonWriter.writeChar(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        char value = function.applyAsChar(object);
        writeFieldName(jsonWriter);
        jsonWriter.writeChar(value);
        return true;
    }
}
