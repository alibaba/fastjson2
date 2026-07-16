package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderDouble<T, V>
        extends FieldReaderBoxedType<T, Double> {
    FieldReaderDouble(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer<T, V> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Double value;
        try {
            value = readValue(jsonReader);
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                value = null;
            } else {
                throw e;
            }
        }

        if (value == null && defaultValue != null) {
            return;
        }

        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setObject(object, value);
    }

    @Override
    protected Double readValue(JSONReader jsonReader) {
        return jsonReader.readDouble();
    }
}
