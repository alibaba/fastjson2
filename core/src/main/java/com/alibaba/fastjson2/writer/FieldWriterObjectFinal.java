package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

abstract class FieldWriterObjectFinal<T> extends FieldWriterImpl<T> {
    final Type fieldType;
    final Class fieldClass;
    volatile ObjectWriter objectWriter;
    boolean refDetect;

    protected FieldWriterObjectFinal(String name, int ordinal, long features, String format, Type fieldType, Class fieldClass) {
        super(name, ordinal, features, format, fieldType, fieldClass);
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.refDetect = !ObjectWriterProvider.isNotReferenceDetect(fieldClass);
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (fieldClass != valueClass) {
            return jsonWriter.getObjectWriter(valueClass);
        }

        if (objectWriter != null) {
            return objectWriter;
        } else {
            return objectWriter = jsonWriter.getObjectWriter(valueClass);
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
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);

                if (fieldClass.isArray()) {
                    jsonWriter.writeArrayNull();
                } else {
                    jsonWriter.writeNull();
                }
                return true;
            } else {
                return false;
            }
        }

        ObjectWriter valueWriter = jsonWriter.getObjectWriter(fieldClass);

        writeFieldName(jsonWriter);
        if (jsonWriter.isJSONB()) {
            valueWriter.writeJSONB(jsonWriter, value, name, fieldType, features);
        } else {
            valueWriter.write(jsonWriter, value, name, fieldType, features);
        }

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

            String refPath = jsonWriter.setPath(name, value);
            if (refPath != null) {
                jsonWriter.writeReference(refPath);
                jsonWriter.popPath(value);
                return;
            }
        }

        ObjectWriter valueWriter = getObjectWriter(jsonWriter, fieldClass);
        if (jsonWriter.isJSONB()) {
            valueWriter.writeJSONB(jsonWriter, value, name, fieldType, features);
        } else {
            valueWriter.write(jsonWriter, value, name, fieldType, features);
        }

        if (refDetect) {
            jsonWriter.popPath(value);
        }
    }
}
