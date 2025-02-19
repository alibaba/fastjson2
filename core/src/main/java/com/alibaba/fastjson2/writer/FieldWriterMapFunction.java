package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

final class FieldWriterMapFunction
        extends FieldWriterMap {
    final Function function;
    FieldWriterMapFunction(
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
            Function function,
            Class<?> contentAs
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, contentAs);
        this.function = function;
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        Map value;
        try {
            value = (Map) function.apply(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        throw new UnsupportedOperationException();
    }
}
