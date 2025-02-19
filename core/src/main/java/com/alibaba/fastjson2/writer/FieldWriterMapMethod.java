package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldWriterMapMethod
        extends FieldWriterMap {
    FieldWriterMapMethod(
            String name,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Class<?> contentAs
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, contentAs);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        throw new UnsupportedOperationException();
    }
}
