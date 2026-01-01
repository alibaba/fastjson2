package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.ObjDoubleConsumer;

final class FieldReaderDoubleValue<T>
        extends FieldReaderObject<T> {
    public FieldReaderDoubleValue(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Double defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjDoubleConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setDouble(object, fieldValue);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setDouble(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readDoubleValue();
    }

    @Override
    public void accept(T object, Object value) {
        double doubleValue = TypeUtils.toDoubleValue(value);

        if (schema != null) {
            schema.assertValidate(doubleValue);
        }

        propertyAccessor.setDouble(object, doubleValue);
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setDouble(object, value);
    }

    @Override
    public void accept(T object, double value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setDouble(object, value);
    }
}
