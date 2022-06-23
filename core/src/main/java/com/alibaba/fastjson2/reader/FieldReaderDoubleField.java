package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderDoubleField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderDoubleField(String fieldName, Class fieldType, int ordinal, long features, String format, Double defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Double fieldValue = jsonReader.readDouble();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readDouble();
    }

    @Override
    public void accept(T object, Object value) {
        Double doubleValue = TypeUtils.toDouble(value);

        if (schema != null) {
            schema.assertValidate(doubleValue);
        }

        try {
            field.set(object, doubleValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
