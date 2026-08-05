package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderBigDecimal<T, V>
        extends FieldReaderBoxedType<T, BigDecimal> {
    public FieldReaderBigDecimal(
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
            BiConsumer<T, V> function
    ) {
        super(fieldName, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, null, null);
    }

    @Override
    protected BigDecimal readValue(JSONReader jsonReader) {
        return jsonReader.readBigDecimal();
    }
}
