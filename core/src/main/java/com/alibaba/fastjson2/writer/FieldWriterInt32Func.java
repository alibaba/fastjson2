package com.alibaba.fastjson2.writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterInt32Func<T>
        extends FieldWriterInt32<T> {
    final Function<T, Integer> function;

    FieldWriterInt32Func(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Function<T, Integer> function
    ) {
        super(fieldName, ordinal, features, format, label, Integer.class, Integer.class, field, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
