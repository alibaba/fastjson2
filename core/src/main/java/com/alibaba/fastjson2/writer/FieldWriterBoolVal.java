package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Predicate;

final class FieldWriterBoolVal
        extends FieldWriterBoolean {
    FieldWriterBoolVal(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Predicate function
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, Object object) {
        boolean value;
        try {
            value = propertyAccessor.getBooleanValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (!value) {
            long features = this.features | jsonWriter.getFeatures();
            if (defaultValue == null && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0) {
                return false;
            }
        }

        writeBool(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, Object object) {
        boolean value;
        try {
            value = propertyAccessor.getBooleanValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (!value) {
            long features = this.features | jsonWriter.getFeatures();
            if (defaultValue == null && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0) {
                return false;
            }
        }

        writeBoolUTF8(jsonWriter, value);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, Object object) {
        boolean value;
        try {
            value = propertyAccessor.getBooleanValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (!value) {
            long features = this.features | jsonWriter.getFeatures();
            if (defaultValue == null && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0) {
                return false;
            }
        }

        writeBoolUTF16(jsonWriter, value);
        return true;
    }
}
