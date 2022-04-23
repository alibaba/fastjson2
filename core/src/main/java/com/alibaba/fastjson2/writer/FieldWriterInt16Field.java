package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;

final class FieldWriterInt16Field<T> extends FieldWriterInt16<T> {
    final Field field;

    FieldWriterInt16Field(String name, int ordinal, Field field, Class fieldClass) {
        super(name, ordinal, fieldClass);
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
