package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

abstract class FieldWriterMap
        extends FieldWriter {
    protected final Class<?> contentAs;

    protected FieldWriterMap(
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
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method);
        this.contentAs = contentAs;
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (fieldClass.isAssignableFrom(valueClass)) {
            return ObjectWriterImplMap.of(fieldType, format, valueClass);
        } else {
            return ObjectWriterImplMap.of(valueClass);
        }
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        throw new UnsupportedOperationException();
    }
}
