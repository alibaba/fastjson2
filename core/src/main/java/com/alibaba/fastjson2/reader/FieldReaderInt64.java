package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderInt64<T>
        extends FieldReaderObject<T> {
    FieldReaderInt64(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Long defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long fieldValue;
        try {
            fieldValue = jsonReader.readInt64();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        propertyAccessor.setObject(object, fieldValue);
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
