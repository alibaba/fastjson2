package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Objects;

final class FieldWriterMillisField<T>
        extends FieldWriterDate<T> {
    FieldWriterMillisField(String fieldName,
            int ordinal,
            long features,
            String dateTimeFormat,
            String label,
            Field field
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, label, long.class, long.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldLong(object);
    }

    public long getFieldLong(T object) {
        return propertyAccessor.getLong(Objects.requireNonNull(object));
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long millis = getFieldLong(object);
        writeDate(jsonWriter, millis);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        long millis = getFieldLong(object);
        writeDate(jsonWriter, false, millis);
    }
}
