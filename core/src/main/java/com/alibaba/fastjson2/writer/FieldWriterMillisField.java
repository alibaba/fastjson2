package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;

final class FieldWriterMillisField<T> extends FieldWriterDate<T> {
    final Field field;

    FieldWriterMillisField(String fieldName
            , int ordinal
            , long features
            , String dateTimeFormat
            , Field method
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, long.class, long.class);
        this.field = method;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldLong(object);
    }

    public long getFieldLong(T object) {
        try {
            return field.getLong(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + name, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long millis = getFieldLong(object);
        writeDate(jsonWriter, millis);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        long millis = getFieldLong(object);
        writeDate(jsonWriter, false, millis);
    }
}
