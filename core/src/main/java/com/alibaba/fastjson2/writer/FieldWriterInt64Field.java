package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;

final class FieldWriterInt64Field<T>
        extends FieldWriterInt64<T> {
    FieldWriterInt64Field(String name, int ordinal, long features, String format, String label, Field field) {
        super(name, ordinal, features, format, label, Long.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }
}
