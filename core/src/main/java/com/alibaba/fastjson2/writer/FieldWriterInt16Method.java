package com.alibaba.fastjson2.writer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class FieldWriterInt16Method<T>
        extends FieldWriterInt16<T> {
    FieldWriterInt16Method(
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
