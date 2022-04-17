package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterBoolMethod extends FieldWriterBoolean {
    final Method method;

    protected FieldWriterBoolMethod(String fieldName, int ordinal, long features, Method method, Class fieldClass) {
        super(fieldName, ordinal, features, null, fieldClass, fieldClass);
        this.method = method;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + name, e);
        }
    }
}
