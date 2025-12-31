package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Objects;

final class FieldWriterInt8ValField<T>
        extends FieldWriterInt8<T> {
    FieldWriterInt8ValField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, label, byte.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueByte(object);
    }

    public byte getFieldValueByte(T object) {
        return propertyAccessor.getByte(Objects.requireNonNull(object));
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        byte value = getFieldValueByte(object);
        writeInt8(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        byte value = getFieldValueByte(object);
        jsonWriter.writeInt32(value);
    }
}
