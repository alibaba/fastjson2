package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt64Field<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt64Field(String fieldName, Class fieldType, int ordinal, long features, String format, Long defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long fieldValue = jsonReader.readInt64();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64();
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        try {
            field.set(object, TypeUtils.toLong(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
