package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterInt16Func<T>
        extends FieldWriterInt16<T> {
    final Function<T, Short> function;

    protected FieldWriterInt16Func(String fieldName, int ordinal, long features, String format, String label, Method method, Function<T, Short> function) {
        super(fieldName, ordinal, features, format, label, Short.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }
}
