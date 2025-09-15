package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterDoubleFunc<T>
        extends FieldWriter<T> {
    final Function<T, Double> function;

    FieldWriterDoubleFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Function<T, Double> function
    ) {
        super(fieldName, ordinal, features, format, null, label, Double.class, Double.class, field, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Double value = function.apply(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
        } else {
            double doubleValue = value;
            if (decimalFormat != null) {
                jsonWriter.writeDouble(doubleValue, decimalFormat);
            } else {
                jsonWriter.writeDouble(doubleValue);
            }
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Double value;
        try {
            value = function.apply(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = jsonWriter.getFeatures(this.features);
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullNumberAsZero.mask)) == 0) {
                return false;
            }
            if ((features & JSONWriter.Feature.NotWriteDefaultValue.mask) == 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeDecimalNull(features);
                return true;
            }
            return false;
        }

        writeFieldName(jsonWriter);

        double doubleValue = value;
        if (decimalFormat != null) {
            jsonWriter.writeDouble(doubleValue, decimalFormat);
        } else {
            jsonWriter.writeDouble(doubleValue);
        }
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
