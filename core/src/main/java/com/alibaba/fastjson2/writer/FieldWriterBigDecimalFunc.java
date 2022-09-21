package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.function.Function;

final class FieldWriterBigDecimalFunc<T>
        extends FieldWriter<T> {
    final Function<T, BigDecimal> function;

    protected FieldWriterBigDecimalFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            Function<T, BigDecimal> function
    ) {
        super(fieldName, ordinal, features, format, label, BigDecimal.class, BigDecimal.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        BigDecimal value = function.apply(object);
        jsonWriter.writeDecimal(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        BigDecimal value;
        try {
            value = function.apply(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
                return false;
            }
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeDecimal(value);
        return true;
    }
}
