package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt32ValueField<T> extends FieldReaderObjectField<T> {
    FieldReaderInt32ValueField(String fieldName, Class fieldType, int ordinal, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();
        try {
            field.setInt(object, fieldInt);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        try {
            field.set(object, TypeUtils.toIntValue(value));
        } catch (Exception e) {
            throw new JSONException("set " + getFieldName() + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            field.setInt(object, (int) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
