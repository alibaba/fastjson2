package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderBigInteger<T>
        extends FieldReaderObject<T> {
    FieldReaderBigInteger(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            BigInteger defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer function
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigInteger value;
        try {
            value = jsonReader.readBigInteger();
        } catch (Exception ex) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                value = null;
            } else {
                throw ex;
            }
        }
        propertyAccessor.setObject(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        propertyAccessor.setObject(object, value);
    }

    @Override
    public void accept(T object, int value) {
        propertyAccessor.setIntValue(object, value);
    }

    @Override
    public void accept(T object, long value) {
        propertyAccessor.setLongValue(object, value);
    }
}
