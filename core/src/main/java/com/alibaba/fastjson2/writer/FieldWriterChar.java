package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

import static com.alibaba.fastjson2.JSONWriter.*;

class FieldWriterChar<T>
        extends FieldWriter<T> {
    FieldWriterChar(
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

    public Object getFieldValue(T object) {
        return propertyAccessor.getObject(object);
    }

    public char getFieldValueChar(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }
        return propertyAccessor.getCharValue(object);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Character value = (Character) propertyAccessor.getObject(object);
        if (value == null) {
            jsonWriter.writeNull();
        } else {
            jsonWriter.writeChar(value);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Character value;
        try {
            value = (Character) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }
        if (value == null) {
            if ((features & (MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE)) != 0) {
                writeFieldName(jsonWriter);
                if ((features & MASK_NULL_AS_DEFAULT_VALUE) == 0) {
                    jsonWriter.writeNull();
                } else {
                    jsonWriter.writeString("\u0000");
                }
                return true;
            }
            return false;
        }
        return writeChar(jsonWriter, value);
    }

    protected final boolean writeChar(JSONWriter jsonWriter, char value) {
        if (value == '\0' && (jsonWriter.getFeatures(features) & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return false;
        }
        writeFieldName(jsonWriter);
        jsonWriter.writeChar(value);
        return true;
    }
}
