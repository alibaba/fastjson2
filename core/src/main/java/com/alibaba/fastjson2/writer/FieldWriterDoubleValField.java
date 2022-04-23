package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

final class FieldWriterDoubleValField<T> extends FieldWriterImpl<T> {
    final Field field;

    FieldWriterDoubleValField(String name, int ordinal, Field field) {
        super(name, ordinal, 0, null, double.class, double.class);
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueDouble(object);
    }

    public double getFieldValueDouble(T object) {
        try {
            return field.getDouble(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + name, e);
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
