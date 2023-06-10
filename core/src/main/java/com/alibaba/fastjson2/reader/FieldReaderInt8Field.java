package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt8Field<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt8Field(String fieldName, Class fieldType, int ordinal, long features, String format, Byte defaultValue, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Integer fieldInt = jsonReader.readInt32();
        try {
            field.set(object, fieldInt == null ? null : fieldInt.byteValue());
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, short value) {
        accept(object, Byte.valueOf((byte) value));
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Byte.valueOf((byte) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Byte.valueOf((byte) value));
    }

    @Override
    public void accept(T object, int value) {
        accept(object, Byte.valueOf((byte) value));
    }

    @Override
    public void accept(T object, long value) {
        accept(object, Byte.valueOf((byte) value));
    }

    @Override
    public void accept(T object, Object value) {
        Byte byteValue = TypeUtils.toByte(value);
        try {
            field.set(object, byteValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (byte) jsonReader.readInt32Value();
    }
}
