package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderInt64ValueMethod<T> extends FieldReaderObjectMethod<T> {
    FieldReaderInt64ValueMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, setter);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldLong = jsonReader.readInt64Value();
        try {
            method.invoke(object, fieldLong);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            value = 0L;
        }

        try {
            method.invoke(object
                    , TypeUtils.toLongValue(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            method.invoke(object, (long) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64Value();
    }
}
