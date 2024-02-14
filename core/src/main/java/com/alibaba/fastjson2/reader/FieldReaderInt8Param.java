package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderInt8Param<T>
        extends FieldReaderObjectParam<T> {
    FieldReaderInt8Param(
            String fieldName,
            Class fieldType,
            String paramName,
            Parameter parameter,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema) {
        super(fieldName, fieldType, fieldType, paramName, parameter, ordinal, features, format, locale, defaultValue, schema);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Integer integer = jsonReader.readInt32();
        return integer == null ? null : integer.byteValue();
    }
}
