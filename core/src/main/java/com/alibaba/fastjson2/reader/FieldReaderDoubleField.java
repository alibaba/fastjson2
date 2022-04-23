package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;

final class FieldReaderDoubleField<T> extends FieldReaderObjectField<T> {
    FieldReaderDoubleField(String fieldName, Class fieldType, int ordinal, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Double fieldValue = jsonReader.readDouble();
        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readDouble();
    }
}
