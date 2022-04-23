package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterInt8Func<T> extends FieldWriterInt8<T> {
    final Method method;
    final Function<T, Byte> function;

    FieldWriterInt8Func(String fieldName, int ordinal, Method method, Function<T, Byte> function) {
        super(fieldName, ordinal, Byte.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Object getFieldValue(T object) {
        return function.apply(object);
    }

    @Override
    public Method getMethod() {
        return method;
    }
}
