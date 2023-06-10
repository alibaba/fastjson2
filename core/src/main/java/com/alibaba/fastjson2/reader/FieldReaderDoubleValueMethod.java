package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldReaderDoubleValueMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderDoubleValueMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Locale locale, Double defaultValue, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        double doubleValue = TypeUtils.toDoubleValue(value);
        try {
            method.invoke(object,
                    doubleValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            method.invoke(object, (double) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
