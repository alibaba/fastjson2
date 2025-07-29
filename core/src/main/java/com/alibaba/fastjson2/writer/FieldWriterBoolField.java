package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class FieldWriterBoolField
        extends FieldWriterBoolean {
    protected FieldWriterBoolField(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Class fieldClass
    ) {
        super(fieldName, ordinal, features, format, label, fieldClass, fieldClass, field, method);
    }

    public Object getFieldValue(Object object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }
}
