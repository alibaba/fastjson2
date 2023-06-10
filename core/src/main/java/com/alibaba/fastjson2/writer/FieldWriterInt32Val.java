package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.reflect.Field;

final class FieldWriterInt32Val<T>
        extends FieldWriterInt32<T> {
    FieldWriterInt32Val(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field
    ) {
        super(name, ordinal, features, format, label, int.class, int.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        return getFieldValueInt(object);
    }

    public int getFieldValueInt(T object) {
        if (object == null) {
            throw new JSONException("field.get error, " + fieldName);
        }

        try {
            int value;
            if (fieldOffset != -1) {
                value = JDKUtils.UNSAFE.getInt(object, fieldOffset);
            } else {
                value = field.getInt(object);
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        int value = getFieldValueInt(object);

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue)) {
            return false;
        }

        writeInt32(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        int value = getFieldValueInt(object);
        jsonWriter.writeInt32(value);
    }
}
