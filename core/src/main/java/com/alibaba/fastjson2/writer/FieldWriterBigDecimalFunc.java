package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.function.Function;

final class FieldWriterBigDecimalFunc<T> extends FieldWriterImpl<T> {
    final Method method;
    final Function<T, BigDecimal> function;

    protected FieldWriterBigDecimalFunc(
            String fieldName
            , int ordinal
            , long features
            , Method method
            , Function<T, BigDecimal> function
    ) {
        super(fieldName, ordinal, features, null, BigDecimal.class, BigDecimal.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
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
