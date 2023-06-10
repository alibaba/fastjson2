package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

final class FieldWriterStringField<T>
        extends FieldWriter<T> {
    protected FieldWriterStringField(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(fieldName, ordinal, features, format, label, String.class, String.class, field, null);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        String value = (String) getFieldValue(object);

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) == 0
                    || (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0) {
                return false;
            }

            if ((features & (JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeString("");
                return true;
            }
        }

        if (trim && value != null) {
            value = value.trim();
        }

        writeFieldName(jsonWriter);

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
        String value = (String) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        if (trim) {
            value = value.trim();
        }

        if (raw) {
            jsonWriter.writeRaw(value);
        } else {
            jsonWriter.writeString(value);
        }
    }
}
