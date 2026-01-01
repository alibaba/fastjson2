package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

final class FieldWriterFloat
        extends FieldWriter {
    FieldWriterFloat(
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
        Float value;
        try {
            value = (Float) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeFieldName(jsonWriter);

        float floatValue = value;
        if (decimalFormat != null) {
            jsonWriter.writeFloat(floatValue, decimalFormat);
        } else {
            if ((features & WriteNonStringValueAsString.mask) != 0) {
                jsonWriter.writeString(floatValue);
            } else {
                jsonWriter.writeFloat(floatValue);
            }
        }

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
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
