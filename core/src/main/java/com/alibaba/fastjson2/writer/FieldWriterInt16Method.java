package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterInt16Method<T> extends FieldWriterInt16<T> {
    final Method method;

    protected FieldWriterInt16Method(String fieldName, int ordinal, Method method, Class fieldClass) {
        super(fieldName, ordinal, fieldClass);
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
}
