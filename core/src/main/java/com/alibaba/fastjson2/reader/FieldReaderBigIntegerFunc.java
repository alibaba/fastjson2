package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderBigIntegerFunc<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    public FieldReaderBigIntegerFunc(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, V> function
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, format, locale, defaultValue, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Object value) {
        BigInteger bigInteger = TypeUtils.toBigInteger(value);

        if (schema != null) {
            schema.assertValidate(bigInteger);
        }

        function.accept(object, (V) bigInteger);
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object,
                (V) BigInteger.valueOf(value));
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, (V) BigInteger.valueOf(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigInteger fieldValue = jsonReader.readBigInteger();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        function.accept(object, (V) fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBigInteger();
    }
}
