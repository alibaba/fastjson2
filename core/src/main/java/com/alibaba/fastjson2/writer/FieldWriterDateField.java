package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.util.Date;

final class FieldWriterDateField<T> extends FieldWriterDate<T> {
    final Field field;

    protected FieldWriterDateField(String fieldName, int ordinal, long features, String format, Field field) {
        super(fieldName, ordinal, features, format, Date.class, Date.class);
        this.field = field;
    }

    @Override
    public Field getField() {
        return field;
    }

    public Object getFieldValue(Object object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + name, e);
        }
    }

    public void writeValue(JSONWriter jsonWriter, T object) {
        Date value = (Date) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNull();
            return;
        }
        writeDate(jsonWriter, false, value.getTime());
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Date date = (Date) getFieldValue(object);

        if (date == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        writeDate(jsonWriter, date.getTime());
        return true;
    }
}
