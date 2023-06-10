package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldReaderBoolMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderBoolMethod(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Boolean defaultValue,
            Method method
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, method, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Boolean fieldValue = jsonReader.readBool();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        Boolean booleanValue = TypeUtils.toBoolean(value);
        try {
            method.invoke(object, booleanValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
