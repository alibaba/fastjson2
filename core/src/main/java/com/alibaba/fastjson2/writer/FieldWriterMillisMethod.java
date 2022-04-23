package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterMillisMethod<T> extends FieldWriterDate<T> {
    final Method method;

    FieldWriterMillisMethod(String fieldName
            , int ordinal
            , long features
            , String dateTimeFormat
            , Method method
            , Class fieldClass
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, fieldClass, fieldClass);
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
    public boolean write(JSONWriter jsonWriter, T object) {
        long millis = (Long) getFieldValue(object);
        writeDate(jsonWriter, millis);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        long millis = (Long) getFieldValue(object);
        writeDate(jsonWriter, false, millis);
    }
}
