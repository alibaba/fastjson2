package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Parameter;

final class FieldReaderInt64Param<T> extends FieldReaderObjectParam<T> {
    FieldReaderInt64Param(String fieldName, Class fieldType, String paramName, Parameter parameter, int ordinal) {
        super(fieldName, fieldType, fieldType, paramName, parameter, ordinal, 0, null);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64();
    }
}
