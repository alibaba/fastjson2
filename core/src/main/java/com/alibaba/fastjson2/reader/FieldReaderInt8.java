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

final class FieldReaderInt8<T>
        extends FieldReaderObject<T> {
    FieldReaderInt8(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Byte defaultValue,
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
        Integer fieldValue;
        try {
            fieldValue = jsonReader.readInt32();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        if (jsonReader.jsonb) {
            if (fieldValue == null
                    && fieldClass == byte.class
                    && (jsonReader.features(this.features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
                throw new JSONException(jsonReader.info("byte value not support input null"));
            }
        } else {
            if (fieldValue == null && (features & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0 && fieldClass == byte.class) {
                throw new JSONException(jsonReader.info("byte value not support input null"));
            }
        }

        Byte byteValue = fieldValue == null ? null : fieldValue.byteValue();

        if (byteValue == null && defaultValue != null) {
            return;
        }

        propertyAccessor.setObject(object, byteValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Integer value = jsonReader.readInt32();
        if (value == null
                && fieldClass == byte.class
                && (jsonReader.features(this.features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
            throw new JSONException(jsonReader.info("byte value not support input null"));
        }
        return value == null ? null : value.byteValue();
    }

    @Override
    public void accept(T object, Object value) {
        Byte byteValue = TypeUtils.toByte(value);

        propertyAccessor.setObject(object, byteValue);
    }
}
