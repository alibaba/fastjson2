package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;

final class FieldWriterObjectFunc<T> extends FieldWriterObject<T> {
    final Method method;
    final Function function;
    final boolean isArray;

    protected FieldWriterObjectFunc(String name, int ordinal, long features, String format, Type fieldType, Class fieldClass, Method method, Function function) {
        super(name, ordinal, features, format, fieldType, fieldClass);
        this.method = method;
        this.function = function;
        isArray = fieldClass == AtomicIntegerArray.class
                || fieldClass == AtomicLongArray.class
                || fieldClass == AtomicReferenceArray.class
                || fieldClass.isArray();
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.apply(object);
    }
}
