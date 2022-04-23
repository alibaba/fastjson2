package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt16Field<T> extends FieldReaderObjectField<T> {
    FieldReaderInt16Field(String fieldName, Class fieldType, int ordinal, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Integer fieldInt = jsonReader.readInt32();
        try {
            field.set(object, fieldInt == null ? null : fieldInt.shortValue());
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            field.set(object, (short) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            field.set(object, (short) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        try {
            field.set(object
                    , TypeUtils.toShort(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (byte) jsonReader.readInt32Value();
    }
}
