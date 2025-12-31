package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Objects;

final class FieldWriterCharValField<T>
        extends FieldWriter<T> {
    FieldWriterCharValField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, null, label, char.class, char.class, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        return getFieldValueChar(object);
    }

    public char getFieldValueChar(Object object) {
        return propertyAccessor.getChar(Objects.requireNonNull(object));
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        char value = getFieldValueChar(object);

        writeFieldName(jsonWriter);
        jsonWriter.writeChar(value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        char value = getFieldValueChar(object);
        jsonWriter.writeChar(value);
    }
}
