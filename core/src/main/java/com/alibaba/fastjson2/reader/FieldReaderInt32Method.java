package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;

final class FieldReaderInt32Method<T>
        extends FieldReaderObject<T> {
    FieldReaderInt32Method(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Integer defaultValue,
            Method setter
    ) {
        super(fieldName, Integer.class, Integer.class, ordinal, features, format, locale, defaultValue, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Integer fieldValue = jsonReader.readInt32();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        Integer fieldValue = jsonReader.readInt32();
        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        Integer integer = TypeUtils.toInteger(value);
        try {
            method.invoke(object, integer);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32();
    }
}
