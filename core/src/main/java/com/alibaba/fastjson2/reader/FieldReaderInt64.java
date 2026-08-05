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
        extends FieldReaderBoxedType<T, Long> {
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
        super(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void accept(T object, Object value) {
        // Convert to Long for schema validation, but set original value
        // This preserves the original behavior where validation used converted value
        // but the original value was set (to preserve type information)
        Long longValue = TypeUtils.toLong(value);

        if (schema != null) {
            schema.assertValidate(longValue);
        }

        propertyAccessor.setObject(object, value);
    }

    @Override
    protected Long readValue(JSONReader jsonReader) {
        return jsonReader.readInt64();
    }
}
