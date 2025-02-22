package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.function.Function;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldWriterMapFunction
        extends FieldWriterMap {
    final Function function;
    FieldWriterMapFunction(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function function,
            Class<?> contentAs
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method, contentAs);
        this.function = function;
    }

    @Override
    public Object getFieldValue(Object object) {
        return function.apply(object);
    }
}
