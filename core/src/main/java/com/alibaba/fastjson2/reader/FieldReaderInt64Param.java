package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderInt64Param<T>
        extends FieldReaderObjectParam<T> {
    FieldReaderInt64Param(
            String fieldName,
            Class fieldType,
            String paramName,
            Parameter parameter,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema
    ) {
        super(fieldName, fieldType, fieldType, paramName, parameter, ordinal, features, format, locale, defaultValue, schema);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Long value = jsonReader.readInt64();
        if (value == null
                && fieldClass == long.class
                && (jsonReader.features(this.features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
            throw new JSONException(jsonReader.info("long value not support input null"));
        }
        return value;
    }
}
