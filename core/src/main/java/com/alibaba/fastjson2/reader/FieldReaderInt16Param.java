package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Parameter;

final class FieldReaderInt16Param<T> extends FieldReaderObjectParam<T> {
    FieldReaderInt16Param(String fieldName, Class fieldType, String paramName, Parameter parameter, int ordinal) {
        super(fieldName, fieldType, fieldType, paramName, parameter, ordinal, 0, null, null);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Integer integer = jsonReader.readInt32();
        return integer == null ? null : integer.shortValue();
    }
}
