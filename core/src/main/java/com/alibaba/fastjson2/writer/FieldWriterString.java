package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Function;

final class FieldWriterString<T>
        extends FieldWriter<T> {
    FieldWriterString(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function function
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        String value = (String) propertyAccessor.getObject(object);

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
    public boolean write(JSONWriter jsonWriter, T object) {
        String value;
        try {
            value = (String) propertyAccessor.getObject(object);
        } catch (Exception error) {
            if ((jsonWriter.getFeatures(features) | JSONWriter.Feature.IgnoreNonFieldGetter.mask) != 0) {
                return false;
            }
            throw error;
        }

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
}
