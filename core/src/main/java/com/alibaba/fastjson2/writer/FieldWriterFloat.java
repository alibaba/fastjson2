package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.*;

class FieldWriterFloat<T>
        extends FieldWriter<T> {
    FieldWriterFloat(
            String name,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);
    }

    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    public float getFieldValueFloat(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }
        return propertyAccessor.getFloatValue(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Float value = (Float) propertyAccessor.getObject(object);
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

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Float value;
        try {
            value = (Float) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeFloatValue(jsonWriter, value, features);
        return true;
    }

    protected final void writeFloatValue(JSONWriter jsonWriter, Float value, long features) {
        float floatValue = value;
        if (floatValue == 0.0f && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }

        writeFieldName(jsonWriter);

        if (decimalFormat != null) {
            jsonWriter.writeFloat(floatValue, decimalFormat);
        } else {
            boolean writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
            if (writeNonStringValueAsString) {
                jsonWriter.writeString(Float.toString(floatValue));
            } else {
                jsonWriter.writeFloat(floatValue);
            }
        }
    }
}
