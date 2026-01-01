package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderString<T, V>
        extends FieldReaderObject<T> {
    final boolean trim;
    final boolean upper;
    final boolean emptyToNull;

    public FieldReaderString(
            String fieldName,
            Class<V> fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer<T, V> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, method, field,
                function, paramName, parameter);
        trim = "trim".equals(format) || (features & JSONReader.Feature.TrimString.mask) != 0;
        upper = "upper".equals(format);
        emptyToNull = (features & JSONReader.Feature.EmptyStringAsNull.mask) != 0;
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        return fieldValue;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();
        if (fieldValue != null) {
            if (trim) {
                fieldValue = fieldValue.trim();
            }
            if (upper) {
                fieldValue = fieldValue.toUpperCase();
            }
            if (emptyToNull && fieldValue.isEmpty()) {
                fieldValue = null;
            }
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();
        if (fieldValue != null) {
            if (trim) {
                fieldValue = fieldValue.trim();
            }
            if (upper) {
                fieldValue = fieldValue.toUpperCase();
            }
            if (emptyToNull && fieldValue.isEmpty()) {
                fieldValue = null;
            }
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        accept(object, fieldValue);
    }

    @Override
    public void accept(T object, int value) {
        accept(object, Integer.toString(value));
    }

    @Override
    public void accept(T object, long value) {
        accept(object, Long.toString(value));
    }

    @Override
    public void accept(T object, Object value) {
        String fieldValue;
        if (value instanceof String || value == null) {
            fieldValue = (String) value;
        } else {
            fieldValue = value.toString();
        }

        if (fieldValue != null) {
            if (trim) {
                fieldValue = fieldValue.trim();
            }
            if (upper) {
                fieldValue = fieldValue.toUpperCase();
            }
            if (emptyToNull && fieldValue.isEmpty()) {
                fieldValue = null;
            }
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        // The propertyAccessor internally handles both field and functional access
        propertyAccessor.setObject(object, fieldValue);
    }

    public boolean supportAcceptType(Class valueClass) {
        return true;
    }
}
