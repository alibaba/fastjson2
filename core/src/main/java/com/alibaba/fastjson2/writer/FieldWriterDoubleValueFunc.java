package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToDoubleFunction;

import java.lang.reflect.Method;

final class FieldWriterDoubleValueFunc
        extends FieldWriter {
    final ToDoubleFunction function;

    protected FieldWriterDoubleValueFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            ToDoubleFunction function
    ) {
        super(fieldName, ordinal, features, format, label, double.class, double.class, null, method);
        this.function = function;
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
            jsonWriter.writeDouble(value);
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

        writeFieldName(jsonWriter);
        if (decimalFormat != null) {
            jsonWriter.writeDouble(value, decimalFormat);
        } else {
            jsonWriter.writeDouble(value);
        }
        return true;
    }
}
