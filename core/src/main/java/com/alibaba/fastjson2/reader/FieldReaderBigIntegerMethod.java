package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Locale;

final class FieldReaderBigIntegerMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderBigIntegerMethod(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            BigInteger defaultValue,
            Method method
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, method, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigInteger fieldValue = jsonReader.readBigInteger();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        BigInteger bigInteger = TypeUtils.toBigInteger(value);
        try {
            method.invoke(object, bigInteger);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            method.invoke(object, BigInteger.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            method.invoke(object, BigInteger.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
