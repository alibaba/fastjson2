package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt32Field<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt32Field(String fieldName, Class fieldType, int ordinal, long features, String format, Integer defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Integer fieldValue = jsonReader.readInt32();

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
        return jsonReader.readInt32();
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Integer.valueOf((int) value));
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Integer.valueOf((int) value));
    }

    @Override
    public void accept(T object, Object value) {
        Integer integer = TypeUtils.toInteger(value);
        if (schema != null) {
            schema.assertValidate(integer);
        }

        if (value == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
            return;
        }

        try {
            field.set(object, integer);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
