package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

final class FieldWriterFloatValField<T>
        extends FieldWriter<T> {
    FieldWriterFloatValField(String name, int ordinal, String format, String label, Field field) {
        super(name, ordinal, 0, format, label, float.class, float.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueFloat(object);
    }

    public float getFieldValueFloat(T object) {
        try {
            return field.getFloat(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
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
        jsonWriter.writeFloat(value);
    }
}
