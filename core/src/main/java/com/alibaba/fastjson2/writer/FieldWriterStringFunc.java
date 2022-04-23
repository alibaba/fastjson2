package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterStringFunc<T> extends FieldWriterImpl<T> {
    final Method method;
    Function<T, String> function;
    final boolean symbol;
    final boolean trim;

    protected FieldWriterStringFunc(
            String fieldName
            , int ordinal
            , long features
            , String format
            , Method method
            , Function<T, String> function
    ) {
        super(fieldName, ordinal, features, format, String.class, String.class);
        this.method = method;
        this.function = function;
        this.symbol = "symbol".equals(format);
        this.trim = "trim".equals(format);
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
    public boolean write(JSONWriter jsonWriter, T object) {
        String value;
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

        if (value == null && (features & JSONWriter.Feature.NullAsDefaultValue.mask) != 0) {
            jsonWriter.writeString("");
            return true;
        }

        if (trim && value != null) {
            value = value.trim();
        }

        if (symbol && jsonWriter.isJSONB()) {
            jsonWriter.writeSymbol(value);
        } else {
            jsonWriter.writeString(value);
        }
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        String value = function.apply(object);

        if (trim && value != null) {
            value = value.trim();
        }

        if (symbol && jsonWriter.isJSONB()) {
            jsonWriter.writeSymbol(value);
        } else {
            jsonWriter.writeString(value);
        }
    }
}
