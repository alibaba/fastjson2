package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class FieldWriterStringMethod<T>
        extends FieldWriterImpl<T> {
    final boolean symbol;
    final boolean trim;
    final boolean raw;

    FieldWriterStringMethod(
            String fieldName,
            int ordinal,
            String format,
            String label,
            long features,
            Method method
    ) {
        super(fieldName, ordinal, features, format, label, String.class, String.class, null, method);
        this.symbol = "symbol".equals(format);
        this.trim = "trim".equals(format);
        this.raw = (features & FieldInfo.RAW_VALUE_MASK) != 0;
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + name, e);
        }
    }

    @Override
    public void writeString(JSONWriter jsonWriter, String value) {
        writeFieldName(jsonWriter);

        if (value == null && (features & (JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) != 0) {
            jsonWriter.writeString("");
            return;
        }

        if (trim && value != null) {
            value = value.trim();
        }

        if (symbol && jsonWriter.isJSONB()) {
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
    public void writeValue(JSONWriter jsonWriter, T object) {
        String value = (String) getFieldValue(object);

        if (trim && value != null) {
            value = value.trim();
        }

        if (symbol && jsonWriter.isJSONB()) {
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
    public boolean write(JSONWriter jsonWriter, T object) {
        String value;
        try {
            value = (String) getFieldValue(object);
        } catch (JSONException error) {
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

        if (trim && value != null) {
            value = value.trim();
        }

        writeString(jsonWriter, value);
        return true;
    }
}
