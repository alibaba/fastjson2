package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderDoubleValueField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderDoubleValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Double defaultValue, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();
        try {
            field.setDouble(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readDoubleValue();
    }

    @Override
    public void accept(T object, Object value) {
        double doubleValue = TypeUtils.toDoubleValue(value);
        try {
            field.set(object, doubleValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
