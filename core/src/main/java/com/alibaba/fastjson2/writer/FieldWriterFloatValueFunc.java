package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToFloatFunction;

import java.lang.reflect.Method;

final class FieldWriterFloatValueFunc extends FieldWriterImpl {
    final Method method;
    final ToFloatFunction function;

    protected FieldWriterFloatValueFunc(String fieldName, int ordinal, Method method, ToFloatFunction function) {
        super(fieldName, ordinal, 0, null, float.class, float.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.applyAsFloat(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        float fieldValue = function.applyAsFloat(object);
        jsonWriter.writeFloat(fieldValue);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        float value;
        try {
            value = function.applyAsFloat(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeFloat(value);
        return true;
    }
}
