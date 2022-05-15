package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.math.BigInteger;

final class FieldReaderBigIntegerField<T> extends FieldReaderObjectField<T> {
    FieldReaderBigIntegerField(String fieldName, Class fieldType, int ordinal, long features, String format, BigInteger defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigInteger fieldValue = jsonReader.readBigInteger();

        if (schema != null) {
            schema.validate(fieldValue);
        }

        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            field.set(object, BigInteger.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            field.set(object, BigInteger.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        try {
            field.set(object
                    , TypeUtils.toBigInteger(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
