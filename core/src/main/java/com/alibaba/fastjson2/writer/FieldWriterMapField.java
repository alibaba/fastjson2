package com.alibaba.fastjson2.writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldWriterMapField
        extends FieldWriterMap {
    FieldWriterMapField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Class<?> contentAs
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method, contentAs);
    }
}
