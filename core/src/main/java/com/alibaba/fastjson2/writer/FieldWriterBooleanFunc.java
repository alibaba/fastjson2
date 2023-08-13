package com.alibaba.fastjson2.writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

final class FieldWriterBooleanFunc
        extends FieldWriterBoolean {
    final Function function;

    FieldWriterBooleanFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Function function
    ) {
        super(fieldName, ordinal, features, format, label, Boolean.class, Boolean.class, field, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.apply(object);
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
