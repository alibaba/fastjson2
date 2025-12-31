package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Objects;

final class FieldWriterInt32Val<T>
        extends FieldWriterInt32<T> {
    FieldWriterInt32Val(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, label, int.class, int.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueInt(object);
    }

    public int getFieldValueInt(T object) {
        return propertyAccessor.getInt(Objects.requireNonNull(object));
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        int value = getFieldValueInt(object);

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return false;
        }

        writeInt32(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        int value = getFieldValueInt(object);
        jsonWriter.writeInt32(value);
    }
}
