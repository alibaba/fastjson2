package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderBool<T, V>
        extends FieldReaderBoxedType<T, Boolean> {
    public FieldReaderBool(
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
        Boolean boolValue = TypeUtils.toBoolean(value);

        if (schema != null) {
            schema.assertValidate(boolValue);
        }

        propertyAccessor.setObject(object, boolValue);
    }

    @Override
    protected Boolean readValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
