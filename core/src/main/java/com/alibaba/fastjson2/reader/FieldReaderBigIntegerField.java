package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.math.BigInteger;

final class FieldReaderBigIntegerField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderBigIntegerField(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            BigInteger defaultValue,
            Field field
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigInteger fieldValue = jsonReader.readBigInteger();

        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
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
        BigInteger bigInteger = TypeUtils.toBigInteger(value);

        try {
            field.set(object, bigInteger);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
