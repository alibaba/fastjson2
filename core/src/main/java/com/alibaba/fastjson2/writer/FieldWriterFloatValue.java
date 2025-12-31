package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

class FieldWriterFloatValue
        extends FieldWriter {
    FieldWriterFloatValue(
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
    public boolean write(JSONWriter jsonWriter, Object object) {
        float value;
        try {
            value = propertyAccessor.getFloat(object);
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
            jsonWriter.writeFloat(value, decimalFormat);
        } else {
            if ((features & WriteNonStringValueAsString.mask) != 0) {
                jsonWriter.writeString(value);
            } else {
                jsonWriter.writeFloat(value);
            }
        }

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        float value = propertyAccessor.getFloat(object);
        if (decimalFormat != null) {
            jsonWriter.writeFloat(value, decimalFormat);
        } else {
            jsonWriter.writeFloat(value);
        }
    }
}
