package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.alibaba.fastjson2.JSONWriter.Feature.*;

abstract class FieldWriterInt64<T>
        extends FieldWriter<T> {
    final boolean browserCompatible;

    FieldWriterInt64(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldClass, fieldClass, field, method);
        browserCompatible = (features & JSONWriter.Feature.BrowserCompatible.mask) != 0;
    }

    public final void writeInt64(JSONWriter jsonWriter, long value) {
        long features = jsonWriter.getFeatures() | this.features;
        boolean writeAsString = (features & (WriteNonStringValueAsString.mask | WriteLongAsString.mask)) != 0;
        writeFieldName(jsonWriter);
        if (!writeAsString) {
            writeAsString = browserCompatible && !TypeUtils.isJavaScriptSupport(value) && !jsonWriter.jsonb;
        }
        if (writeAsString) {
            jsonWriter.writeString(Long.toString(value));
        } else {
            jsonWriter.writeInt64(value);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Long value;
        try {
            value = (Long) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullNumberAsZero.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeNumberNull();
            return true;
        }

        writeInt64(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Long value = (Long) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeInt64(value);
    }
}
