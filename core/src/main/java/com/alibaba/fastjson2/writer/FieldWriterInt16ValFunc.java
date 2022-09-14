package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToShortFunction;

import java.lang.reflect.Method;

final class FieldWriterInt16ValFunc
        extends FieldWriterInt16 {
    final ToShortFunction function;

    FieldWriterInt16ValFunc(String fieldName, int ordinal, long features, String format, String label, Method method, ToShortFunction function) {
        super(fieldName, ordinal, features, format, label, short.class, null, method);
        this.function = function;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        short value = function.applyAsShort(object);
        jsonWriter.writeInt32(value);
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.applyAsShort(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        short value;
        try {
            value = function.applyAsShort(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        writeInt16(jsonWriter, value);
        return true;
    }
}
