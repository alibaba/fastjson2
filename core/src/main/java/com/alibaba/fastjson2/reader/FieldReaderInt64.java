package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderInt64<T, V>
        extends FieldReader<T> {
    FieldReaderInt64(
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
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void accept(T object, Object value) {
        Long longValue = TypeUtils.toLong(value);

        if (schema != null) {
            schema.assertValidate(longValue);
        }

        propertyAccessor.setObject(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long value;
        try {
            value = jsonReader.readInt64();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                value = null;
            } else {
                throw e;
            }
        }

        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setObject(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64();
    }
}
