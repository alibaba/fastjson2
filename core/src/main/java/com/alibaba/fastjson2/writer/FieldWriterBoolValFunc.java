package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.function.Predicate;

import java.lang.reflect.Method;

final class FieldWriterBoolValFunc
        extends FieldWriterBoolVal {
    final Predicate function;

    protected FieldWriterBoolValFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method,
            Predicate function
    ) {
        super(fieldName, ordinal, features, format, label, Boolean.class, Boolean.class, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.test(object);
    }
}
