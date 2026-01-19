package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;

final class FieldWriterInt64Value<T>
        extends FieldWriter<T> {
    final boolean browserCompatible;

    FieldWriterInt64Value(
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
        browserCompatible = (features & JSONWriter.Feature.BrowserCompatible.mask) != 0;
    }

    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    public long getFieldValueLong(T object) {
        if (object == null) {
            throw new RuntimeException("field.get error, " + fieldName);
        }
        return propertyAccessor.getLongValue(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Long value = (Long) propertyAccessor.getObject(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
        } else {
            jsonWriter.writeInt64(value);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        long value;
        try {
            value = propertyAccessor.getLongValue(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == 0L
                && defaultValue == null
                && (features & MASK_NOT_WRITE_DEFAULT_VALUE) != 0) {
            return false;
        }

        writeLong(jsonWriter, value);
        return true;
    }

    protected final void writeLong(JSONWriter jsonWriter, long value) {
        long features = jsonWriter.getFeatures() | this.features;
        boolean writeAsString = (features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;
        if (!writeAsString) {
            writeAsString = browserCompatible && !TypeUtils.isJavaScriptSupport(value) && !jsonWriter.jsonb;
        }
        writeFieldName(jsonWriter);
        if (writeAsString) {
            jsonWriter.writeString(Long.toString(value));
        } else {
            jsonWriter.writeInt64(value);
        }
    }
}
