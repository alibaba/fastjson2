package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Objects;

final class FieldWriterDoubleValField<T>
        extends FieldWriter<T> {
    FieldWriterDoubleValField(String name, int ordinal, String format, String label, Field field) {
        super(name, ordinal, 0, format, null, label, double.class, double.class, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        return getFieldValueDouble(object);
    }

    public double getFieldValueDouble(Object object) {
        return propertyAccessor.getDouble(Objects.requireNonNull(object));
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
        if (decimalFormat != null) {
            jsonWriter.writeDouble(value, decimalFormat);
        } else {
            jsonWriter.writeDouble(value);
        }
    }
}
