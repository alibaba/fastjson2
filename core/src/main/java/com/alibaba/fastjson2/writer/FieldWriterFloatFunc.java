package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.Function;

import java.lang.reflect.Method;

final class FieldWriterFloatFunc<T>
        extends FieldWriter<T> {
    final Function<T, Float> function;

    protected FieldWriterFloatFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            Function<T, Float> function
    ) {
        super(fieldName, ordinal, features, format, label, Float.class, Float.class, null, method);
        this.function = function;
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
            float floatValue = value.floatValue();
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
        float floatValue = value.floatValue();
        if (decimalFormat != null) {
            jsonWriter.writeFloat(floatValue, decimalFormat);
        } else {
            jsonWriter.writeFloat(floatValue);
        }
        return true;
    }
}
