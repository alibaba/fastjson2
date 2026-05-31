package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;

final class FieldWriterDoubleValue<T>
        extends FieldWriterDouble<T> {
    FieldWriterDoubleValue(
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

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        double value;
        try {
            value = propertyAccessor.getDoubleValue(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == 0.0
                && defaultValue == null
                && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
            return false;
        }

        writeDoubleValue(jsonWriter, value, features);
        return true;
    }

    private void writeDoubleValue(JSONWriter jsonWriter, double value, long features) {
        if (value == 0.0 && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }

        writeFieldName(jsonWriter);

        if (decimalFormat != null) {
            jsonWriter.writeDouble(value, decimalFormat);
        } else {
            boolean writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
            if (writeNonStringValueAsString) {
                jsonWriter.writeString(Double.toString(value));
            } else {
                jsonWriter.writeDouble(value);
            }
        }
    }
}
