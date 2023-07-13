package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

final class FieldWriterBoolValField
        extends FieldWriterBoolVal {
    FieldWriterBoolValField(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Class fieldClass
    ) {
        super(fieldName, ordinal, features, format, label, fieldClass, fieldClass, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        return getFieldValueBoolean(object);
    }

    public boolean getFieldValueBoolean(Object object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            boolean value;
            if (fieldOffset != -1) {
                value = UNSAFE.getBoolean(object, fieldOffset);
            } else {
                value = field.getBoolean(object);
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }
}
