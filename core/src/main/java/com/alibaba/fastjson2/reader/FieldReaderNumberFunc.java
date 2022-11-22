package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderNumberFunc<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    public FieldReaderNumberFunc(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Number defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, V> function
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, (V) value);
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, (V) Integer.valueOf(value));
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, (V) Long.valueOf(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Number fieldValue;
        try {
            fieldValue = jsonReader.readNumber();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readNumber();
    }
}
