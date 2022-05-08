package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderInt64Method<T> extends FieldReaderObjectMethod<T> {
    FieldReaderInt64Method(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Long defaultValue, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, setter);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long fieldValue = jsonReader.readInt64();
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
                    , TypeUtils.toLong(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
