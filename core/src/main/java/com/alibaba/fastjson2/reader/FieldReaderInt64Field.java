package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt64Field<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt64Field(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Long defaultValue,
            JSONSchema schema,
            Field field
    ) {
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
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64();
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Long.valueOf((long) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Long.valueOf((long) value));
    }

    @Override
    public void accept(T object, Object value) {
        Long longValue = TypeUtils.toLong(value);

        if (schema != null) {
            schema.assertValidate(longValue);
        }

        try {
            field.set(object, longValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
