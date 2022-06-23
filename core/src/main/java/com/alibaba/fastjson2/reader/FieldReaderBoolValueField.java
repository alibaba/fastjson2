package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderBoolValueField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderBoolValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Boolean defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        boolean fieldValue = jsonReader.readBoolValue();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        try {
            field.setBoolean(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, int value) {
        accept(object, TypeUtils.toBooleanValue(value));
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
