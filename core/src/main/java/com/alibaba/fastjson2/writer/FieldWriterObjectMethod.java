package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

class FieldWriterObjectMethod<T>
        extends FieldWriterObject<T> {
    final Method method;

    protected FieldWriterObjectMethod(
            String name
            , int ordinal
            , long features
            , String format
            , Type fieldType
            , Class fieldClass
            , Method method
    ) {
        super(name, ordinal, features, format, fieldType, fieldClass);
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
