package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderDateFunc<T>
        extends FieldReaderImplDate<T> {
    final BiConsumer<T, Date> function;

    public FieldReaderDateFunc(
            String fieldName,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Date defaultValue,
            JSONSchema schema,
            Method method,
            BiConsumer<T, Date> function) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null);
        this.function = function;
    }

    @Override
    public void accept(T object, Date value) {
        function.accept(object, value);
    }
}
