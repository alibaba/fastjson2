package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.util.function.Predicate;

final class FieldWriterBoolValFunc
        extends FieldWriterBoolVal {
    final Predicate function;

    protected FieldWriterBoolValFunc(String fieldName, int ordinal, long features, String format, String label, Method method, Predicate function) {
        super(fieldName, ordinal, features, format, label, Boolean.class, Boolean.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.test(object);
    }
}
