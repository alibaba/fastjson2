package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterInt64Func<T>
        extends FieldWriterInt64<T> {
    final Function<T, Long> function;

    protected FieldWriterInt64Func(String fieldName, int ordinal, long features, String format, String label, Method method, Function<T, Long> function) {
        super(fieldName, ordinal, features, format, label, Long.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }
}
