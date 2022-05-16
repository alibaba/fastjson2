package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONSchema;

import java.lang.reflect.Field;

final class FieldReaderInt64ValueField<T> extends FieldReaderObjectField<T> {
    FieldReaderInt64ValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Long defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldLong = jsonReader.readInt64Value();

        if (schema != null) {
            schema.validate(fieldLong);
        }

        try {
            field.setLong(object, fieldLong);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
