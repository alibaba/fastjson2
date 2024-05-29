package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterStringFunc<T>
        extends FieldWriter<T> {
    final Function<T, String> function;
    final boolean symbol;
    final boolean trim;
    final boolean raw;

    FieldWriterStringFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Function<T, String> function
    ) {
        super(fieldName, ordinal, features, format, null, label, String.class, String.class, field, method);
        this.function = function;
        this.symbol = "symbol".equals(format);
        this.trim = "trim".equals(format);
        this.raw = (features & FieldInfo.RAW_VALUE_MASK) != 0;
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
            if ((jsonWriter.getFeatures(features) | JSONWriter.Feature.IgnoreNonFieldGetter.mask) != 0) {
                return false;
            }
            throw error;
        }

        long features = this.features | jsonWriter.getFeatures();
        if (value == null) {
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) == 0) {
                return false;
            }
        } else if (trim) {
            value = value.trim();
        }

        if (value != null
                && value.isEmpty()
                && (features & JSONWriter.Feature.IgnoreEmpty.mask) != 0
        ) {
            return false;
        }

        writeFieldName(jsonWriter);

        if (value == null) {
            if ((features & (JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) != 0) {
                jsonWriter.writeString("");
            } else {
                jsonWriter.writeNull();
            }
            return true;
        }

        if (symbol && jsonWriter.jsonb) {
            jsonWriter.writeSymbol(value);
        } else {
            if (raw) {
                jsonWriter.writeRaw(value);
            } else {
                jsonWriter.writeString(value);
            }
        }
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        String value = function.apply(object);

        if (trim && value != null) {
            value = value.trim();
        }

        if (symbol && jsonWriter.jsonb) {
            jsonWriter.writeSymbol(value);
        } else {
            if (raw) {
                jsonWriter.writeRaw(value);
            } else {
                jsonWriter.writeString(value);
            }
        }
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
