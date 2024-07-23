package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

class FieldReaderStringField<T>
        extends FieldReaderObjectField<T> {
    final boolean trim;
    final boolean upper;
    final boolean emptyToNull;

    FieldReaderStringField(String fieldName, Class fieldType, int ordinal, long features, String format, String defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, null, defaultValue, schema, field);
        trim = "trim".equals(format) || (features & JSONReader.Feature.TrimString.mask) != 0;
        upper = "upper".equals(format);
        emptyToNull = (features & JSONReader.Feature.EmptyStringAsNull.mask) != 0;
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

        UNSAFE.putObject(object, fieldOffset, fieldValue);
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
    public String readFieldValue(JSONReader jsonReader) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        return fieldValue;
    }

    public boolean supportAcceptType(Class valueClass) {
        return true;
    }

    @Override
    public void accept(T object, Object value) {
        String fieldValue;
        if (value != null && !(value instanceof String)) {
            fieldValue = value.toString();
        } else {
            fieldValue = (String) value;
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

        UNSAFE.putObject(object, fieldOffset, fieldValue);
    }
}
