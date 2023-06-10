package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.reflect.Field;

final class FieldWriterCharValField<T>
        extends FieldWriter<T> {
    FieldWriterCharValField(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, label, char.class, char.class, field, null);
    }

    @Override
    public Object getFieldValue(Object object) {
        return getFieldValueChar(object);
    }

    public char getFieldValueChar(Object object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            char value;
            if (fieldOffset != -1) {
                value = JDKUtils.UNSAFE.getChar(object, fieldOffset);
            } else {
                value = field.getChar(object);
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        char value = getFieldValueChar(object);

        writeFieldName(jsonWriter);
        jsonWriter.writeChar(value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        char value = getFieldValueChar(object);
        jsonWriter.writeChar(value);
    }
}
