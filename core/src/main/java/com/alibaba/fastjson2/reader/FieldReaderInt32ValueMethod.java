package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderInt32ValueMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderInt32ValueMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Integer defaultValue, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, defaultValue, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();

        try {
            method.invoke(object, fieldInt);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();

        try {
            method.invoke(object, fieldInt);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        int intValue = TypeUtils.toIntValue(value);

        try {
            method.invoke(object, intValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            method.invoke(object, (int) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
