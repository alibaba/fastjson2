package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterInt64Method<T> extends FieldWriterInt64<T> {
    final Method method;

    protected FieldWriterInt64Method(String fieldName, int ordinal, long features, String format, Method method, Class fieldClass) {
        super(fieldName, ordinal, features, format, fieldClass);
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
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw new JSONException("invoke getter method error, " + name, cause != null ? cause : e);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("invoke getter method error, " + name, e);
        }
    }
}
