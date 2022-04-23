package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

final class FIeldWriterEnumField extends FieldWriterEnum {
    final Field field;

    protected FIeldWriterEnumField(String name, int ordinal, String format, long features, Class fieldType, Field field) {
        super(name, ordinal, features, format, fieldType);
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + name, e);
        }
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        Enum value = (Enum) getFieldValue(object);
        jsonWriter.writeEnum(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        Enum value = (Enum) getFieldValue(object);

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        if (jsonWriter.isJSONB()) {
            writeEnumJSONB(jsonWriter, value);
        } else {
            writeEnum(jsonWriter, value);
        }
        return true;
    }
}
