package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterMillisMethod<T>
        extends FieldWriterDate<T> {
    FieldWriterMillisMethod(
            String fieldName,
            int ordinal,
            long features,
            String dateTimeFormat,
            String label,
            Class fieldClass,
            Method method
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, label, fieldClass, fieldClass, null, method);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + fieldName, e);
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
