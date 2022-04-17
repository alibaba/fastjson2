package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterBooleanFunc extends FieldWriterBoolean {
    final Method method;
    final Function function;

    protected FieldWriterBooleanFunc(String fieldName, int ordinal, long features, Method method, Function function) {
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
        return function.apply(object);
    }
}
