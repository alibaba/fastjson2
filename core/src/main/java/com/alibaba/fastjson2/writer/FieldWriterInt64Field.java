package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;

final class FieldWriterInt64Field<T> extends FieldWriterInt64<T> {
    final Field field;

    FieldWriterInt64Field(String name, int ordinal, Field field) {
        super(name, ordinal, 0, null, Long.class);
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
