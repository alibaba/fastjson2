package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

final class FieldWriterStringField<T> extends FieldWriterImpl<T> {
    final Field field;
    final boolean symbol;
    final boolean trim;

    protected FieldWriterStringField(
            String fieldName
            , int ordinal
            , String format
            , long features
            , Field field) {

        super(fieldName, ordinal, features, format, String.class, String.class);
        this.field = field;
        this.symbol = "symbol".equals(format);
        this.trim = "trim".equals(format);
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + name, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        String value = (String) getFieldValue(object);

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0
                    || (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0) {
                return false;
            }

            if (value == null && (features & JSONWriter.Feature.NullAsDefaultValue.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeString("");
                return true;
            }
        }

        if (trim && value != null) {
            value = value.trim();
        }

        writeString(jsonWriter, value);
        return true;
    }

    @Override
    public void writeString(JSONWriter jsonWriter, String value) {
        writeFieldName(jsonWriter);

        if (trim) {
            value = value.trim();
        }

        if (symbol && jsonWriter.isJSONB()) {
            jsonWriter.writeSymbol(value);
        } else {
            jsonWriter.writeString(value);
        }
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        String value = (String) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        if (trim) {
            value = value.trim();
        }

        jsonWriter.writeString(value);
    }
}
