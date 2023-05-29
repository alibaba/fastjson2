package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;

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
        super(name, ordinal, features, format, label, Float.class, Float.class, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            Object value;
            if (fieldOffset != -1 && !fieldClass.isPrimitive()) {
                value = UnsafeUtils.getObject(object, fieldOffset);
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
        Float value = (Float) getFieldValue(object);
        if (value == null) {
            long features = jsonWriter.getFeatures(this.features);
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0
                    && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) == 0
            ) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNumberNull();
                return true;
            }
            return false;
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
