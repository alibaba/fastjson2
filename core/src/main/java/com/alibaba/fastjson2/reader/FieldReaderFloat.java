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

final class FieldReaderFloat<T>
        extends FieldReaderObject<T> {
    FieldReaderFloat(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Float defaultValue,
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
        Float fieldValue;
        try {
            fieldValue = jsonReader.readFloat();
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                fieldValue = null;
            } else {
                throw e;
            }
        }

        if (jsonReader.jsonb) {
            if (fieldValue == null
                    && fieldClass == float.class
                    && (jsonReader.features(this.features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
                throw new JSONException(jsonReader.info("float value not support input null"));
            }
        } else {
            if (fieldValue == null && (features & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0 && fieldClass == float.class) {
                throw new JSONException(jsonReader.info("float value not support input null"));
            }
        }

        if (fieldValue == null && defaultValue != null) {
            return;
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        Float value = jsonReader.readFloat();
        if (value == null
                && fieldClass == float.class
                && (jsonReader.features(this.features) & JSONReader.Feature.ErrorOnNullForPrimitives.mask) != 0) {
            throw new JSONException(jsonReader.info("float value not support input null"));
        }
        return value;
    }

    @Override
    public void accept(T object, Object value) {
        Float floatValue = TypeUtils.toFloat(value);

        propertyAccessor.setObject(object, floatValue);
    }
}
