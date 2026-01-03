package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderDouble<T>
        extends FieldReaderObject<T> {
    FieldReaderDouble(
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
            BiConsumer function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Double fieldValue;
        try {
            fieldValue = jsonReader.readDouble();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        if (fieldValue == null && defaultValue != null) {
            return;
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Double value = jsonReader.readDouble();
        if (value == null
                && fieldClass == double.class
                && (jsonReader.features(this.features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
            throw new JSONException(jsonReader.info("double value not support input null"));
        }
        return value;
    }

    @Override
    public void accept(T object, Object value) {
        Double doubleValue = TypeUtils.toDouble(value);

        propertyAccessor.setObject(object, doubleValue);
    }
}
