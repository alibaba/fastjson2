package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;
import static com.alibaba.fastjson2.JSONWriter.MASK_NOT_WRITE_DEFAULT_VALUE;

final class FieldWriterBoolValue<T>
        extends FieldWriterBool<T> {
    FieldWriterBoolValue(
            String name,
            int ordinal,
            long features,
            String format,
            java.util.Locale locale,
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
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        boolean value;
        try {
            value = propertyAccessor.getBooleanValue(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (!value
                && defaultValue == null
                && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
            return false;
        }

        writeBool(jsonWriter, value);
        return true;
    }
}
