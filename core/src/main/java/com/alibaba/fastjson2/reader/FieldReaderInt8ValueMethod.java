package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldReaderInt8ValueMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderInt8ValueMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Locale locale, Byte defaultValue, JSONSchema schema, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();

        if (schema != null) {
            schema.assertValidate(fieldInt);
        }

        propertyAccessor.setByte(object, (byte) fieldInt);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();

        if (schema != null) {
            schema.assertValidate(fieldInt);
        }

        propertyAccessor.setByte(object, (byte) fieldInt);
    }

    @Override
    public void accept(T object, Object value) {
        byte byteValue = TypeUtils.toByteValue(value);

        if (schema != null) {
            schema.assertValidate(byteValue);
        }

        propertyAccessor.setByte(object, byteValue);
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setByte(object, (byte) value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (byte) jsonReader.readInt32Value();
    }
}
