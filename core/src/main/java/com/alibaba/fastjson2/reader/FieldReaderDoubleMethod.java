package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderDoubleMethod<T> extends FieldReaderObjectMethod<T> {
    FieldReaderDoubleMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, setter);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Double fieldValue = jsonReader.readDouble();
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
                    , TypeUtils.toDouble(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
