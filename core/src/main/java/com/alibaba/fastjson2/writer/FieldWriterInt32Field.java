package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;

final class FieldWriterInt32Field<T>
        extends FieldWriterInt32<T> {
    FieldWriterInt32Field(String name, int ordinal, long features, String format, String label, Field field) {
        super(name, ordinal, features, format, label, Integer.class, Integer.class, field, null);
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
