package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Parameter;

final class FieldReaderInt16Param<T>
        extends FieldReaderObjectParam<T> {
    FieldReaderInt16Param(String fieldName, Class fieldType, String paramName, Parameter parameter, int ordinal, long features, String format, JSONSchema schema) {
        super(fieldName, fieldType, fieldType, paramName, parameter, ordinal, features, format, schema);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Integer integer = jsonReader.readInt32();
        return integer == null ? null : integer.shortValue();
    }
}
