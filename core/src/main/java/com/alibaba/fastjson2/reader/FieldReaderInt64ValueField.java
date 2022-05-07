package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;

final class FieldReaderInt64ValueField<T> extends FieldReaderObjectField<T> {
    FieldReaderInt64ValueField(String fieldName, Class fieldType, int ordinal, String format, Long defaultValue, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, 0, format, defaultValue, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldLong = jsonReader.readInt64Value();
        try {
            field.setLong(object, fieldLong);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
