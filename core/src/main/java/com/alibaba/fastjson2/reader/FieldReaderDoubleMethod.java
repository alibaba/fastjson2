package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderDoubleMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderDoubleMethod(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Double defaultValue,
            JSONSchema schema,
            Method setter
    ) {
        super(fieldName, Double.class, Double.class, ordinal, features, format, null, defaultValue, schema, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Double fieldValue = jsonReader.readDouble();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        if (fieldValue == null && defaultValue != null) {
            return;
        }

        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        Double doubleValue = TypeUtils.toDouble(value);

        if (schema != null) {
            schema.assertValidate(doubleValue);
        }

        try {
            method.invoke(object, doubleValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
