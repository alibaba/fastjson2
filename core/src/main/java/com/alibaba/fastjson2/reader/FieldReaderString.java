package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderString<T, V>
        extends FieldReader<T> {
    final boolean trim;
    final boolean upper;
    final boolean emptyToNull;

    FieldReaderString(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer<T, V> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);

        trim = "trim".equals(format) || (features & JSONReader.Feature.TrimString.mask) != 0;
        upper = "upper".equals(format);
        emptyToNull = (features & JSONReader.Feature.EmptyStringAsNull.mask) != 0;
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

        try {
            propertyAccessor.setObject(object, fieldValue);
        } catch (Exception e) {
            if (e instanceof JSONException) {
                throw e;
            }
            throw new JSONException("set " + fieldName + " error", e);
        }
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

        try {
            propertyAccessor.setObject(object, fieldValue);
        } catch (Exception e) {
            if (e instanceof JSONException) {
                throw e;
            }
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
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

        try {
            propertyAccessor.setObject(object, fieldValue);
        } catch (Exception e) {
            if (e instanceof JSONException) {
                throw e;
            }
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        return fieldValue;
    }

    public boolean supportAcceptType(Class valueClass) {
        return true;
    }
}
