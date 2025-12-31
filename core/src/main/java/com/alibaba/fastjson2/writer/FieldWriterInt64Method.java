package com.alibaba.fastjson2.writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class FieldWriterInt64Method<T>
        extends FieldWriterInt64<T> {
    FieldWriterInt64Method(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            Class fieldClass
    ) {
        super(fieldName, ordinal, features, format, label, fieldClass, field, method);
    }
}
