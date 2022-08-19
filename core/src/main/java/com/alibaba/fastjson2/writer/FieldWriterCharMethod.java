package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterCharMethod<T>
        extends FieldWriterImpl<T> {
    final Method method;

    protected FieldWriterCharMethod(String fieldName, int ordinal, long features, String format, String label, Method method, Class fieldClass) {
        super(fieldName, ordinal, features, format, label, fieldClass, fieldClass);
        this.method = method;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + name, e);
        }
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Character value = (Character) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeChar(value.charValue());
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Character value = (Character) getFieldValue(object);

        if (value == null) {
            return false;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeChar(value.charValue());
        return true;
    }
}
