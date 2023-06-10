package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

final class FieldReaderInt8Param<T>
        extends FieldReaderObjectParam<T> {
    FieldReaderInt8Param(String fieldName, Class fieldType, String paramName, int ordinal, long features, String format) {
        super(fieldName, fieldType, fieldType, paramName, ordinal, features, format);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Integer integer = jsonReader.readInt32();
        return integer == null ? null : integer.byteValue();
    }
}
