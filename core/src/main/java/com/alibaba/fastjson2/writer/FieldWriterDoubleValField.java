package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;

final class FieldWriterDoubleValField<T>
        extends FieldWriter<T> {
    FieldWriterDoubleValField(String name, int ordinal, String format, String label, Field field) {
        super(name, ordinal, 0, format, label, double.class, double.class, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        return getFieldValueDouble(object);
    }

    public double getFieldValueDouble(Object object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            double value;
            if (fieldOffset != -1) {
                value = UnsafeUtils.getDouble(object, fieldOffset);
            } else {
                value = field.getDouble(object);
            }
            return value;
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
