package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;

final class FieldWriterInt32Field<T> extends FieldWriterInt32<T> {
    final Field field;

    FieldWriterInt32Field(String name, int ordinal, long features, String format, Field field) {
        super(name, ordinal, features, format, Integer.class, Integer.class);
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + name, e);
        }
    }
}
