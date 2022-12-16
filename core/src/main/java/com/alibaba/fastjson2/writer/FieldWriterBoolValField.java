package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;

final class FieldWriterBoolValField
        extends FieldWriterBoolVal {
    protected FieldWriterBoolValField(String fieldName, int ordinal, long features, String format, String label, Field field, Class fieldClass) {
        super(fieldName, ordinal, features, format, label, fieldClass, fieldClass, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return field.getBoolean(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    protected Boolean getValue(Object object) {
        try {
            return field.getBoolean(object);
        } catch (IllegalAccessException e) {
            throw new JSONException("get field error", e);
        }
    }
}
