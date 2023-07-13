package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

final class FieldWriterInt16ValField<T>
        extends FieldWriterInt16<T> {
    FieldWriterInt16ValField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, label, short.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueShort(object);
    }

    public short getFieldValueShort(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            short value;
            if (fieldOffset != -1) {
                value = UNSAFE.getShort(object, fieldOffset);
            } else {
                value = field.getShort(object);
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        short value = getFieldValueShort(object);
        writeInt16(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        short value = getFieldValueShort(object);
        jsonWriter.writeInt32(value);
    }
}
