package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

class FieldWriterDoubleVal<T>
        extends FieldWriter<T> {
    @SuppressWarnings("unchecked")
    FieldWriterDoubleVal(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        double value;
        try {
            value = propertyAccessor.getDouble(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return false;
        }

        writeFieldName(jsonWriter);
        if (decimalFormat != null) {
            jsonWriter.writeDouble(value, decimalFormat);
        } else {
            if ((features & WriteNonStringValueAsString.mask) != 0) {
                jsonWriter.writeString(value);
            } else {
                jsonWriter.writeDouble(value);
            }
        }
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        double value = propertyAccessor.getDouble(object);

        if (decimalFormat != null) {
            jsonWriter.writeDouble(value, decimalFormat);
        } else {
            if ((features & WriteNonStringValueAsString.mask) != 0) {
                jsonWriter.writeString(value);
            } else {
                jsonWriter.writeDouble(value);
            }
        }
    }
}
