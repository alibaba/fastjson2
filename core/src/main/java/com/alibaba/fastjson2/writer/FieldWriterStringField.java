package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

/**
 * FieldWriterStringField handles serialization of String fields accessed directly via reflection.
 * This is a specialized field writer optimized for String type fields.
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>String field serialization with proper escaping</li>
 *   <li>Null string handling with WriteNullStringAsEmpty feature</li>
 *   <li>String trimming when format is set to "trim"</li>
 *   <li>Empty string filtering with IgnoreEmpty feature</li>
 *   <li>Symbol table optimization for JSONB format</li>
 *   <li>Raw value writing when RAW_VALUE feature is enabled</li>
 * </ul>
 *
 * <p>This implementation uses direct field access via reflection for optimal performance.
 *
 * @param <T> the type of the object containing the string field
 * @since 2.0.0
 */
final class FieldWriterStringField<T>
        extends FieldWriter<T> {
    FieldWriterStringField(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(fieldName, ordinal, features, format, null, label, String.class, String.class, field, null);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        String value = (String) getFieldValue(object);

        long features = this.features | jsonWriter.getFeatures();
        if (value == null) {
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) == 0
                    || (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0) {
                return false;
            }

            writeFieldName(jsonWriter);
            if ((features & (JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullStringAsEmpty.mask)) != 0) {
                jsonWriter.writeString("");
            } else {
                jsonWriter.writeNull();
            }
            return true;
        }

        if (trim) {
            value = value.trim();
        }

        if (value.isEmpty() && (features & JSONWriter.Feature.IgnoreEmpty.mask) != 0) {
            return false;
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
