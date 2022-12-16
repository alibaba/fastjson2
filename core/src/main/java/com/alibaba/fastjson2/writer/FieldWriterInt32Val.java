package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

class FieldWriterInt32Val<T>
        extends FieldWriterInt32<T> {
    FieldWriterInt32Val(String name, int ordinal, long features, String format, String label, Field field) {
        super(name, ordinal, features, format, label, int.class, int.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        int value;
        try {
            value = field.getInt(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue)) {
            return false;
        }

        writeInt32(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        try {
            int value = field.getInt(object);
            jsonWriter.writeInt32(value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }
}
