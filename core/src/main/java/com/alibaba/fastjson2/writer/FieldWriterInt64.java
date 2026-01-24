package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.*;

class FieldWriterInt64<T>
        extends FieldWriter<T> {
    final boolean browserCompatible;

    FieldWriterInt64(
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
        browserCompatible = (features & Feature.BrowserCompatible.mask) != 0;
    }

    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
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
        Long value;
        try {
            value = (Long) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & (MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE | MASK_WRITE_NULL_NUMBER_AS_ZERO)) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeInt64Null(features);
                return true;
            }
            return false;
        }

        writeInt64(jsonWriter, value);
        return true;
    }

    public final void writeInt64(JSONWriter jsonWriter, long value) {
        long features = jsonWriter.getFeatures() | this.features;
        if (value == 0 && (features & NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return;
        }
        boolean writeAsString = (features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;
        writeFieldName(jsonWriter);
        if (!writeAsString) {
            writeAsString = browserCompatible && !TypeUtils.isJavaScriptSupport(value) && !jsonWriter.jsonb;
        }
        if (writeAsString) {
            jsonWriter.writeString(value);
        } else {
            jsonWriter.writeInt64(value);
        }
    }
}
