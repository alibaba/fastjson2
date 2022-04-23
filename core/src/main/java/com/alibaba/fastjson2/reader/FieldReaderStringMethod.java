package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderStringMethod<T> extends FieldReaderObjectMethod<T> {
    final boolean trim;

    FieldReaderStringMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, setter);
        trim = "trim".equals(format);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public String readFieldValue(JSONReader jsonReader) {
        String fieldValue = jsonReader.readString();
        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }
        return fieldValue;
    }
}
