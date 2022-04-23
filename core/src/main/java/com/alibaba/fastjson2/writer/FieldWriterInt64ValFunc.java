package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.util.function.ToLongFunction;

final class FieldWriterInt64ValFunc<T> extends FieldWriterInt64<T> {
    final Method method;
    final ToLongFunction function;

    protected FieldWriterInt64ValFunc(String fieldName, int ordinal, long features, String format, Method method, ToLongFunction function) {
        super(fieldName, ordinal, features, format, long.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.applyAsLong(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long value;
        try {
            value = function.applyAsLong(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        writeInt64(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        long value = function.applyAsLong(object);
        jsonWriter.writeInt64(value);
    }
}
