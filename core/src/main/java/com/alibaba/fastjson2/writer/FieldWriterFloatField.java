package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Objects;

class FieldWriterFloatField<T>
        extends FieldWriter<T> {
    protected FieldWriterFloatField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, null, label, Float.class, Float.class, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        return propertyAccessor.getObject(Objects.requireNonNull(object));
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Float value = (Float) getFieldValue(object);
        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeFieldName(jsonWriter);

        float floatValue = value;
        if (decimalFormat != null) {
            jsonWriter.writeFloat(floatValue, decimalFormat);
        } else {
            jsonWriter.writeFloat(floatValue);
        }

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Float value = (Float) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNumberNull();
        } else {
            float floatValue = value;
            if (decimalFormat != null) {
                jsonWriter.writeFloat(floatValue, decimalFormat);
            } else {
                jsonWriter.writeFloat(floatValue);
            }
        }
    }
}
