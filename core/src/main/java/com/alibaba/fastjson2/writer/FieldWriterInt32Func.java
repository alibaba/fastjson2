package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterInt32Func<T>
        extends FieldWriterInt32<T> {
    final Function<T, Integer> function;

    protected FieldWriterInt32Func(String fieldName, int ordinal, long features, String format, String label, Method method, Function<T, Integer> function) {
        super(fieldName, ordinal, features, format, label, Integer.class, Integer.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }
}
