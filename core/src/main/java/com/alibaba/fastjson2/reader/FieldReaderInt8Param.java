package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.util.Locale;

final class FieldReaderInt8Param<T>
        extends FieldReaderObjectParam<T> {
    FieldReaderInt8Param(String fieldName, Class fieldType, String paramName, int ordinal, long features, String format, Locale locale, Object defaultValue) {
        super(fieldName, fieldType, fieldType, paramName, ordinal, features, format, locale, defaultValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Integer integer = jsonReader.readInt32();
        return integer == null ? null : integer.byteValue();
    }
}
