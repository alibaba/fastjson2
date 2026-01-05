package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.*;
import static com.alibaba.fastjson2.JSONWriter.Feature.BeanToArray;

class FieldWriterObjectFinal<T>
        extends FieldWriterObject<T> {
    final Type fieldType;
    final Class fieldClass;
    volatile ObjectWriter objectWriter;
    final boolean refDetect;

    protected FieldWriterObjectFinal(
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
            Function function
    ) {
        super(name, ordinal, features, format, locale, label, fieldType, fieldClass, field, method, function);
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.refDetect = !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (fieldClass != valueClass) {
            return super.getObjectWriter(jsonWriter, valueClass);
        }

        if (objectWriter != null) {
            return objectWriter;
        } else {
            return objectWriter = super.getObjectWriter(jsonWriter, valueClass);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Object value;
        try {
            value = getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            if (fieldClass.isArray()) {
                jsonWriter.writeArrayNull();
            } else if (fieldClass == StringBuffer.class || fieldClass == StringBuilder.class) {
                jsonWriter.writeStringNull();
            } else {
                jsonWriter.writeObjectNull(fieldClass);
            }
            return true;
        }

        ObjectWriter valueWriter = getObjectWriter(jsonWriter, fieldClass);

        if (unwrapped
                && writeWithUnwrapped(jsonWriter, value, features, refDetect, valueWriter)) {
            return true;
        }

        writeFieldName(jsonWriter);
        if (jsonWriter.jsonb) {
            valueWriter.writeJSONB(jsonWriter, value, fieldName, fieldType, features);
        } else {
            valueWriter.write(jsonWriter, value, fieldName, fieldType, features);
        }

        return true;
    }

    private static final long MAKS_WRITE_NULLS = MASK_WRITE_MAP_NULL_VALUE | MASK_NULL_AS_DEFAULT_VALUE;

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = this.features | jsonWriter.getFeatures();
        Object value;
        try {
            value = getFieldValue(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            if ((features & MAKS_WRITE_NULLS) == 0) {
                return false;
            }
            writeFieldNameUTF8(jsonWriter);
            if (fieldClass.isArray()) {
                jsonWriter.writeArrayNull();
            } else if (fieldClass == StringBuffer.class || fieldClass == StringBuilder.class) {
                jsonWriter.writeStringNull();
            } else {
                jsonWriter.writeObjectNull(fieldClass);
            }
            return true;
        }

        ObjectWriter valueWriter = getObjectWriter(jsonWriter, fieldClass);

        if (unwrapped
                && writeWithUnwrapped(jsonWriter, value, features, refDetect, valueWriter)) {
            return true;
        }

        jsonWriter.writeNameRaw(fieldNameUTF8(features));
        valueWriter.writeUTF8(jsonWriter, value, fieldName, fieldType, features);

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Object value = getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }

        boolean refDetect = this.refDetect && jsonWriter.isRefDetect();

        if (refDetect) {
            if (value == object) {
                jsonWriter.writeReference("..");
                return;
            }

            String refPath = jsonWriter.setPath(fieldName, value);
            if (refPath != null) {
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return;
            }
        }

        ObjectWriter valueWriter = getObjectWriter(jsonWriter, fieldClass);

        boolean beanToArray = (jsonWriter.getFeatures(features) & BeanToArray.mask) != 0;
        if (jsonWriter.jsonb) {
            if (beanToArray) {
                valueWriter.writeArrayMappingJSONB(jsonWriter, value, fieldName, fieldType, features);
            } else {
                valueWriter.writeJSONB(jsonWriter, value, fieldName, fieldType, features);
            }
        } else {
            if (beanToArray) {
                valueWriter.writeArrayMapping(jsonWriter, value, fieldName, fieldType, features);
            } else {
                valueWriter.write(jsonWriter, value, fieldName, fieldType, features);
            }
        }

        if (refDetect) {
            jsonWriter.popPath(value);
        }
    }
}
