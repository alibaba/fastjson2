package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.Function;

import java.lang.reflect.Method;
import java.math.BigDecimal;

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
        jsonWriter.writeDecimal(value, features, decimalFormat);
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
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeDecimal(value, features, decimalFormat);
        return true;
    }
}
