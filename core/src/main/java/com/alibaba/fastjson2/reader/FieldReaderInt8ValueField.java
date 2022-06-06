package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt8ValueField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt8ValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Byte defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();

        if (schema != null) {
            schema.assertValidate(fieldInt);
        }

        try {
            field.setByte(object, (byte) fieldInt);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        try {
            field.setByte(object, (byte) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        try {
            field.setByte(object, (byte) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        try {
            field.setByte(object,
                    TypeUtils.toByteValue(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (byte) jsonReader.readInt32Value();
    }
}
