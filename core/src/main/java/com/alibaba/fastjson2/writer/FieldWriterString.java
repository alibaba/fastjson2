package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.*;

final class FieldWriterString<T>
        extends FieldWriter<T> {
    FieldWriterString(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Field field,
            Method method,
            Function function
    ) {
        super(fieldName, ordinal, features, format, locale, label, String.class, String.class, field, method, function);
    }

    @Override
    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
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
        long features = this.features | jsonWriter.getFeatures();
        String value;
        try {
            value = (String) propertyAccessor.getObject(object);
        } catch (Exception error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & (MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_STRING_AS_EMPTY)) == 0
                    || (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
                return false;
            }
        } else if (trim) {
            value = value.trim();
        }

        if (value != null
                && value.isEmpty()
                && (features & MASK_NOT_WRITE_EMPTY_ARRAY) != 0
        ) {
            return false;
        }

        writeFieldName(jsonWriter);

        if (value == null) {
            if ((features & (MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_STRING_AS_EMPTY)) != 0) {
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
}
