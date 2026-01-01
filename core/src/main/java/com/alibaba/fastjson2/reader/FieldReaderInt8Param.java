package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderInt8Param<T>
        extends FieldReaderObject<T> {
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
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, null, null, null, paramName, parameter);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Integer integer = jsonReader.readInt32();
        if (integer == null) {
            if (fieldClass == byte.class && (jsonReader.features(features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
                throw new JSONException(paramName.concat(" is null"));
            }
            return null;
        }
        return integer.byteValue();
    }
}
