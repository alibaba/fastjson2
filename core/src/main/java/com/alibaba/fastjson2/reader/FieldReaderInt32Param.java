package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

final class FieldReaderInt32Param<T>
        extends FieldReaderObjectParam<T> {
    FieldReaderInt32Param(String fieldName, Class fieldType, String paramName, int ordinal, long features, String format) {
        super(fieldName, fieldType, fieldType, paramName, ordinal, features, format);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32();
    }
}
