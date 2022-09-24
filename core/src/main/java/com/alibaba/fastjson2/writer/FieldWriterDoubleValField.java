package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

final class FieldWriterDoubleValField<T>
        extends FieldWriter<T> {
    FieldWriterDoubleValField(String name, int ordinal, String format, String label, Field field) {
        super(name, ordinal, 0, format, label, double.class, double.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueDouble(object);
    }

    public double getFieldValueDouble(T object) {
        try {
            return field.getDouble(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        double value = getFieldValueDouble(object);
        writeDouble(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        double value = getFieldValueDouble(object);
        jsonWriter.writeDouble(value);
    }
}
