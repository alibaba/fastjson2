package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ToDoubleFunction;

final class FieldWriterDoubleValueFunc
        extends FieldWriter {
    final ToDoubleFunction function;
    final boolean writeNonStringValueAsString;

    FieldWriterDoubleValueFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            ToDoubleFunction function
    ) {
        super(fieldName, ordinal, features, format, null, label, double.class, double.class, field, method);
        this.function = function;
        writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.applyAsDouble(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        double value = function.applyAsDouble(object);
        if (decimalFormat != null) {
            jsonWriter.writeDouble(value, decimalFormat);
        } else {
            if (writeNonStringValueAsString) {
                jsonWriter.writeString(value);
            } else {
                jsonWriter.writeDouble(value);
            }
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        double value;
        try {
            value = function.applyAsDouble(object);
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
            if (writeNonStringValueAsString) {
                jsonWriter.writeString(value);
            } else {
                jsonWriter.writeDouble(value);
            }
        }
        return true;
    }
}
