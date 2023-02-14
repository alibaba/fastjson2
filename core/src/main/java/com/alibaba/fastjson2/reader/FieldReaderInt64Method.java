package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;

final class FieldReaderInt64Method<T>
        extends FieldReaderObject<T> {
    FieldReaderInt64Method(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Long defaultValue,
            JSONSchema schema,
            Method setter
    ) {
        super(fieldName, Long.class, Long.class, ordinal, features, format, locale, defaultValue, schema, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long fieldValue = jsonReader.readInt64();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        Long fieldValue = jsonReader.readInt64();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        try {
            method.invoke(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        Long longValue = TypeUtils.toLong(value);

        if (schema != null) {
            schema.assertValidate(longValue);
        }

        try {
            method.invoke(object, longValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
