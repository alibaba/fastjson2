package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Locale;

final class FieldReaderBigDecimalMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderBigDecimalMethod(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            BigDecimal defaultValue,
            JSONSchema schema,
            Method method
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigDecimal fieldValue = jsonReader.readBigDecimal();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        BigDecimal decimalValue = TypeUtils.toBigDecimal(value);

        if (schema != null) {
            schema.assertValidate(decimalValue);
        }

        propertyAccessor.setObject(object, decimalValue);
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setObject(object, BigDecimal.valueOf(value));
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setObject(object, BigDecimal.valueOf(value));
    }
}
