package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;

final class FieldWriterBoolValField
        extends FieldWriterBoolVal {
    protected FieldWriterBoolValField(
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
                value = UnsafeUtils.getBoolean(object, fieldOffset);
            } else {
                value = field.getBoolean(object);
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }
}
