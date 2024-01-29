package com.alibaba.fastjson2.writer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Function;

final class FieldWriterEnumFunc
        extends FieldWriterEnum {
    final Function function;

    protected FieldWriterEnumFunc(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Method method,
            Function function
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, null, method);
        this.function = function;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.apply(object);
    }
}
