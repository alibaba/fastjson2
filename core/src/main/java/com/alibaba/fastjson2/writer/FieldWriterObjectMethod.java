package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

class FieldWriterObjectMethod<T>
        extends FieldWriterObject<T> {
    protected FieldWriterObjectMethod(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method);
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + fieldName, e);
        }
    }
}
