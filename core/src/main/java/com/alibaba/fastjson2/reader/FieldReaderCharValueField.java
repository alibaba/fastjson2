package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;

final class FieldReaderCharValueField<T>
        extends FieldReader<T> {
    FieldReaderCharValueField(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Character defaultValue,
            Field field
    ) {
        super(fieldName, char.class, char.class, ordinal, features, format, null, defaultValue, null, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        char ch = jsonReader.readCharValue();
        if (ch == '\0' && jsonReader.wasNull()) {
            return;
        }

        accept(object, ch);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readCharValue();
    }

    @Override
    public void accept(T object, Object value) {
        char charValue;
        if (value instanceof String) {
            charValue = ((String) value).charAt(0);
        } else if (value instanceof Character) {
            charValue = (Character) value;
        } else {
            throw new JSONException("cast to char error");
        }
        try {
            field.set(object, charValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
