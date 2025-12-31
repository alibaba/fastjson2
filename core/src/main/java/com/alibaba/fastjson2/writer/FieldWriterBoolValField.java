package com.alibaba.fastjson2.writer;

import java.lang.reflect.Field;
import java.util.Objects;

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
        return propertyAccessor.getBoolean(Objects.requireNonNull(object));
    }
}
