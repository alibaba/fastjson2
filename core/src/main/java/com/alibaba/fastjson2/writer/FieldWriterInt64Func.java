package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterInt64Func<T> extends FieldWriterInt64<T> {
    final Method method;
    final Function<T, Long> function;

    protected FieldWriterInt64Func(String fieldName, int ordinal, Method method, Function<T, Long> function) {
        super(fieldName, ordinal, 0, null, Long.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }
}
