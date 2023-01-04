package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;

final class FieldReaderCharValueField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderCharValueField(String fieldName, int ordinal, long features, String format, Character defaultValue, JSONSchema schema, Field field) {
        super(fieldName, char.class, char.class, ordinal, features, format, defaultValue, schema, field);
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
        String str = jsonReader.readString();
        return str == null || str.isEmpty() ? '\0' : str.charAt(0);
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
        accept(object, charValue);
    }
}
