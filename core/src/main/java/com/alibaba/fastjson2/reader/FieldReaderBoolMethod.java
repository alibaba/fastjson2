package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderBoolMethod<T> extends FieldReaderObjectMethod<T> {
    FieldReaderBoolMethod(String fieldName, Type fieldType, Method setter) {
        super(fieldName, fieldType, setter);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Boolean fieldValue = jsonReader.readBool();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
