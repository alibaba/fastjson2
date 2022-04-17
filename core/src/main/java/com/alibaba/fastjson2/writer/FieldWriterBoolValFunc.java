package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.util.function.Predicate;

final class FieldWriterBoolValFunc extends FieldWriterBoolVal {
    final Method method;
    final Predicate function;

    protected FieldWriterBoolValFunc(String fieldName, int ordinal, long features, Method method, Predicate function) {
        super(fieldName, ordinal, features, null, Boolean.class, Boolean.class);
        this.method = method;
        this.function = function;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.test(object);
    }
}
