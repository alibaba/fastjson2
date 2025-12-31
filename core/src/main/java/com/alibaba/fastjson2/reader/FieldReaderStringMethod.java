package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldReaderStringMethod<T>
        extends FieldReaderObject<T> {
    final boolean trim;
    final boolean upper;

    FieldReaderStringMethod(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String defaultValue,
            JSONSchema schema,
            Method setter
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, setter, null, null);
        trim = "trim".equals(format) || (features & JSONReader.Feature.TrimString.mask) != 0;
        upper = "upper".equals(format);
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
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    @Override
    public String readFieldValue(JSONReader jsonReader) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        return fieldValue;
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
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    public boolean supportAcceptType(Class valueClass) {
        return true;
    }
}
