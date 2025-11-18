package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterFloatFunc<T>
        extends FieldWriter<T> {
    final Function<T, Float> function;
    final boolean writeNonStringValueAsString;

    FieldWriterFloatFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Function<T, Float> function
    ) {
        super(fieldName, ordinal, features, format, null, label, Float.class, Float.class, field, method);
        this.function = function;
        writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Float value = function.apply(object);
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
        Float value;
        try {
            value = function.apply(object);
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
            if (writeNonStringValueAsString) {
                jsonWriter.writeString(floatValue);
            } else {
                jsonWriter.writeFloat(floatValue);
            }
        }
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
