package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

class FieldWriterObjectField<T>
        extends FieldWriterObject<T> {
    protected FieldWriterObjectField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }
}
