package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Objects;

class FieldWriterDoubleField<T>
        extends FieldWriter<T> {
    protected FieldWriterDoubleField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, null, label, Double.class, Double.class, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        return propertyAccessor.getObject(Objects.requireNonNull(object));
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Double value = (Double) getFieldValue(object);

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeFieldName(jsonWriter);

        double doubleValue = value;
        if (decimalFormat != null) {
            jsonWriter.writeDouble(doubleValue, decimalFormat);
        } else {
            jsonWriter.writeDouble(doubleValue);
        }

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Double value = (Double) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNumberNull();
        } else {
            double doubleValue = value;
            if (decimalFormat != null) {
                jsonWriter.writeDouble(doubleValue, decimalFormat);
            } else {
                jsonWriter.writeDouble(doubleValue);
            }
        }
    }
}
