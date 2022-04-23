package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.util.function.ToDoubleFunction;

final class FieldWriterDoubleValueFunc extends FieldWriterImpl {
    final Method method;
    final ToDoubleFunction function;

    protected FieldWriterDoubleValueFunc(String fieldName, int ordinal, Method method, ToDoubleFunction function) {
        super(fieldName, ordinal, 0, null, double.class, double.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.applyAsDouble(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        double fieldValue = function.applyAsDouble(object);
        jsonWriter.writeDouble(fieldValue);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        double value;
        try {
            value = function.applyAsDouble(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeDouble(value);
        return true;
    }
}
