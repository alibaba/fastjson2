package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.reflect.Field;

class FieldWriterDoubleField<T>
        extends FieldWriter<T> {
    final boolean writeNonStringValueAsString;
    protected FieldWriterDoubleField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, label, Double.class, Double.class, field, null);
        writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
    }

    @Override
    public Object getFieldValue(Object object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            Object value;
            if (fieldOffset != -1 && !fieldClass.isPrimitive()) {
                value = JDKUtils.UNSAFE.getObject(object, fieldOffset);
            } else {
                value = field.get(object);
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Double value = (Double) getFieldValue(object);

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeFieldName(jsonWriter);

        double doubleValue = value.doubleValue();
        if (decimalFormat != null) {
            jsonWriter.writeDouble(doubleValue, decimalFormat);
        } else {
            if (writeNonStringValueAsString) {
                jsonWriter.writeString(doubleValue);
            } else {
                jsonWriter.writeDouble(doubleValue);
            }
        }

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Double value = (Double) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNumberNull();
        } else {
            double doubleValue = value.doubleValue();
            if (decimalFormat != null) {
                jsonWriter.writeDouble(doubleValue, decimalFormat);
            } else {
                jsonWriter.writeDouble(doubleValue);
            }
        }
    }
}
