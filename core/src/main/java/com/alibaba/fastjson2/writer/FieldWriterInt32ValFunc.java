package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.util.function.ToIntFunction;

final class FieldWriterInt32ValFunc extends FieldWriterInt32 {
    final Method method;
    final ToIntFunction function;

    protected FieldWriterInt32ValFunc(String fieldName, int ordinal, long features, String format, Method method, ToIntFunction function) {
        super(fieldName, ordinal, features, format, int.class, int.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.applyAsInt(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        int value;
        try {
            value = function.applyAsInt(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        writeInt32(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        int value = function.applyAsInt(object);
        jsonWriter.writeInt32(value);
    }
}
