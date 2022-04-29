package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;

final class FieldReaderBigDecimalMethod<T> extends FieldReaderObjectMethod<T> {
    FieldReaderBigDecimalMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, method);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigDecimal fieldValue = jsonReader.readBigDecimal();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        try {
            method.invoke(object
                    , TypeUtils.toBigDecimal(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            method.invoke(object, BigDecimal.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            method.invoke(object, BigDecimal.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
