package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

abstract class FieldWriterBoolVal
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
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        boolean value;
        try {
            value = (Boolean) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (!value) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0) {
                return false;
            }
        }

        writeBool(jsonWriter, value);
        return true;
    }
}
