package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;

final class FieldWriterInt8ValField<T>
        extends FieldWriterInt8<T> {
    FieldWriterInt8ValField(String name, int ordinal, long features, String format, String label, Field field) {
        super(name, ordinal, features, format, label, byte.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueByte(object);
    }

    public byte getFieldValueByte(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            byte value;
            if (fieldOffset != -1) {
                value = UnsafeUtils.getByte(object, fieldOffset);
            } else {
                value = field.getByte(object);
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
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
