package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

final class FIeldWriterEnumField
        extends FieldWriterEnum {
    protected FIeldWriterEnumField(String name, int ordinal, long features, String format, String label, Class fieldType, Field field) {
        super(name, ordinal, features, format, label, fieldType, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
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

        if (jsonWriter.jsonb) {
            writeEnumJSONB(jsonWriter, value);
        } else {
            writeEnum(jsonWriter, value);
        }
        return true;
    }
}
