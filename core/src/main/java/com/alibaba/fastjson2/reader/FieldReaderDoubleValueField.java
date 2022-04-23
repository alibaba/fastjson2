package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;

final class FieldReaderDoubleValueField<T> extends FieldReaderObjectField<T> {
    FieldReaderDoubleValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();
        try {
            field.setDouble(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readDoubleValue();
    }
}
