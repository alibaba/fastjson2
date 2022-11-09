package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderStringFunc<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    final String format;
    final boolean trim;

    FieldReaderStringFunc(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, V> function
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null);
        this.function = function;

        this.format = format;
        trim = "trim".equals(format) || (features & JSONReader.Feature.TrimString.mask) != 0;
    }

    @Override
    public void accept(T object, int value) {
        accept(object, Integer.toString(value));
    }

    @Override
    public void accept(T object, long value) {
        accept(object, Long.toString(value));
    }

    @Override
    public void accept(T object, Object value) {
        String fieldValue;
        if (value instanceof String || value == null) {
            fieldValue = (String) value;
        } else {
            fieldValue = value.toString();
        }

        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        function.accept(object, (V) fieldValue);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        String fieldValue = jsonReader.readString();

        if (trim && fieldValue != null) {
            fieldValue = fieldValue.trim();
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readString();
    }
}
