package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Objects;

final class FieldWriterFloatValField<T>
        extends FieldWriter<T> {
    FieldWriterFloatValField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, null, label, float.class, float.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueFloat(object);
    }

    public float getFieldValueFloat(T object) {
        return propertyAccessor.getFloat(Objects.requireNonNull(object));
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        float value = getFieldValueFloat(object);
        writeFloat(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        float value = getFieldValueFloat(object);
        if (decimalFormat != null) {
            jsonWriter.writeFloat(value, decimalFormat);
        } else {
            jsonWriter.writeFloat(value);
        }
    }
}
