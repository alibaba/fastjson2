package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterInt16Method<T>
        extends FieldWriterInt16<T> {
    protected FieldWriterInt16Method(String fieldName, int ordinal, long features, String format, String label, Method method, Class fieldClass) {
        super(fieldName, ordinal, features, format, label, fieldClass, null, method);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + fieldName, e);
        }
    }
}
