package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.Locale;

final class FieldReaderInt32Method<T>
        extends FieldReaderObject<T> {
    FieldReaderInt32Method(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Integer defaultValue,
            JSONSchema schema,
            Method setter
    ) {
        super(fieldName, Integer.class, Integer.class, ordinal, features, format, locale, defaultValue, schema, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Integer fieldValue = jsonReader.readInt32();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        Integer fieldValue = jsonReader.readInt32();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        Integer integer = TypeUtils.toInteger(value);

        if (schema != null) {
            schema.assertValidate(integer);
        }

        propertyAccessor.setObject(object, integer);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32();
    }
}
