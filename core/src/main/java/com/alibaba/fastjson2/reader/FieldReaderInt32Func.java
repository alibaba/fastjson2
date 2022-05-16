package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderInt32Func<T, V> extends FieldReaderImpl<T> {
    final Method method;
    final BiConsumer<T, V> function;

    FieldReaderInt32Func(String fieldName, Class<V> fieldClass, int ordinal, String format, Locale locale, Object defaultValue, JSONSchema schema, Method method, BiConsumer<T, V> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, format, locale, defaultValue, schema);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, (V) TypeUtils.toInteger(value));
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Integer fieldValue = jsonReader.readInt32();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        function.accept(object, (V) fieldValue);
    }
}
