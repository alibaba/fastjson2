package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldReaderInt64Method<T> extends FieldReaderObjectMethod<T> {
    FieldReaderInt64Method(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Locale locale, Long defaultValue, JSONSchema schema, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, setter);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long fieldValue = jsonReader.readInt64();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        try {
            method.invoke(object
                    , TypeUtils.toLong(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
