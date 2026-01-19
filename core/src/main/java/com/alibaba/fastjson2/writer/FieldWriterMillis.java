package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.*;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;

final class FieldWriterMillis<T>
        extends FieldWriterDate<T> {
    FieldWriterMillis(
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
            Object function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public Object getFieldValue(T object) {
        return propertyAccessor.getLongValue(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long millis = propertyAccessor.getLongValue(object);
        if (millis == 0) {
            if ((features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
                return false;
            }
        }

        writeDate(jsonWriter, millis);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        long millis = propertyAccessor.getLongValue(object);
        writeDate(jsonWriter, false, millis);
    }
}
