package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToShortFunction;

import java.lang.reflect.Method;

final class FieldWriterInt16ValFunc extends FieldWriterInt16 {
    final Method method;
    final ToShortFunction function;

    FieldWriterInt16ValFunc(String fieldName, int ordinal, Method method, ToShortFunction function) {
        super(fieldName, ordinal, short.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
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
