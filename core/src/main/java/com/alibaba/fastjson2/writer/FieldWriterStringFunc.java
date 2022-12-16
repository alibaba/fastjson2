package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterStringFunc<T>
        extends FieldWriter<T> {
    Function<T, String> function;
    final boolean symbol;
    final boolean trim;
    final boolean raw;

    protected FieldWriterStringFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            Function<T, String> function
    ) {
        super(fieldName, ordinal, features, format, label, String.class, String.class, null, method);
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
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) == 0) {
                return false;
            }
        }
        writeFieldName(jsonWriter);

        if (value == null && (features & (JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) != 0) {
            jsonWriter.writeString("");
            return true;
        }

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
}
