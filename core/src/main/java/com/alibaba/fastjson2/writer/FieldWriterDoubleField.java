package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

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
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            Object value;
            if (fieldOffset != -1 && !fieldClass.isPrimitive()) {
                value = UNSAFE.getObject(object, fieldOffset);
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
            long features = jsonWriter.getFeatures(this.features);
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) != 0
                    && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) == 0
            ) {
                writeFieldName(jsonWriter);
                if ((features & JSONWriter.Feature.NullAsDefaultValue.mask) != 0) {
                    jsonWriter.writeDouble(0);
                } else {
                    jsonWriter.writeNumberNull();
                }
                return true;
            }
            return false;
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
