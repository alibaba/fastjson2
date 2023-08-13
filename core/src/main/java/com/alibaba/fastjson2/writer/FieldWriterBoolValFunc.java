package com.alibaba.fastjson2.writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

final class FieldWriterBoolValFunc
        extends FieldWriterBoolVal {
    final Predicate function;

    FieldWriterBoolValFunc(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Predicate function
    ) {
        super(fieldName, ordinal, features, format, label, Boolean.class, Boolean.class, field, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.test(object);
    }
}
