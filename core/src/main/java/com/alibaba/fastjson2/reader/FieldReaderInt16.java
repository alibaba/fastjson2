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

final class FieldReaderInt16<T>
        extends FieldReaderObject<T> {
    FieldReaderInt16(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Short defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, locale, defaultValue, schema, method, field,
                function, paramName, parameter, null);
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
                    && fieldClass == short.class
                    && (jsonReader.features(this.features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
                throw new JSONException(jsonReader.info("short value not support input null"));
            }
        } else {
            if (fieldValue == null && (features & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0 && fieldClass == short.class) {
                throw new JSONException(jsonReader.info("short value not support input null"));
            }
        }

        Short shortValue = fieldValue == null ? null : fieldValue.shortValue();

        if (schema != null) {
            schema.assertValidate(shortValue);
        }

        if (shortValue == null && defaultValue != null) {
            return;
        }

        propertyAccessor.setObject(object, shortValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Integer value = jsonReader.readInt32();
        if (value == null
                && fieldClass == short.class
                && (jsonReader.features(this.features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
            throw new JSONException(jsonReader.info("short value not support input null"));
        }
        return value == null ? null : value.shortValue();
    }

    @Override
    public void accept(T object, Object value) {
        Short shortValue = TypeUtils.toShort(value);

        if (schema != null) {
            schema.assertValidate(shortValue);
        }

        propertyAccessor.setObject(object, shortValue);
    }
}
