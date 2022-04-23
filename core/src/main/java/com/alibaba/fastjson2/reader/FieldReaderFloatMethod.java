package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderFloatMethod<T> extends FieldReaderObjectMethod<T> {
    FieldReaderFloatMethod(String fieldName, Type fieldType, Method setter) {
        super(fieldName, fieldType, setter);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Float fieldValue = jsonReader.readFloat();
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
                    , TypeUtils.toFloat(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
