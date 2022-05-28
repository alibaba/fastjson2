package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Parameter;

final class FieldReaderInt32Param<T>
        extends FieldReaderObjectParam<T> {
    FieldReaderInt32Param(String fieldName, Class fieldType, String paramName, Parameter parameter, int ordinal, long features, String format, JSONSchema schema) {
        super(fieldName, fieldType, fieldType, paramName, parameter, ordinal, features, format, schema);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32();
    }
}
