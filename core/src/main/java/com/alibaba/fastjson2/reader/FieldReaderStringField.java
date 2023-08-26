package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

class FieldReaderStringField<T>
        extends FieldReaderObjectField<T> {
    final boolean trim;

    FieldReaderStringField(String fieldName, Class fieldType, int ordinal, long features, String format, String defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
        trim = "trim".equals(format) || (features & JSONReader.Feature.TrimString.mask) != 0;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        UNSAFE.putObject(object, fieldOffset, fieldValue);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
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

        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        UNSAFE.putObject(object, fieldOffset, fieldValue);
    }
}
