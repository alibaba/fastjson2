package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterInt64Method<T>
        extends FieldWriterInt64<T> {
    protected FieldWriterInt64Method(String fieldName, int ordinal, long features, String format, String label, Method method, Class fieldClass) {
        super(fieldName, ordinal, features, format, label, fieldClass, null, method);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return method.invoke(object);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw new JSONException("invoke getter method error, " + fieldName, cause != null ? cause : e);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("invoke getter method error, " + fieldName, e);
        }
    }
}
