package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Abstract base class for boxed type field readers (Integer, Long, Double, Float, Boolean, etc.).
 * Consolidates common error handling and schema validation logic.
 *
 * @param <T> the object type containing the field
 * @param <V> the field value type
 */
abstract class FieldReaderBoxedType<T, V> extends FieldReader<T> {
    FieldReaderBoxedType(
            String fieldName,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void accept(T object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }
        propertyAccessor.setObject(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        V value;
        try {
            value = readValue(jsonReader);
        } catch (Exception e) {
            if ((jsonReader.features(this.features) & JSONReader.Feature.NullOnError.mask) != 0) {
                value = null;
            } else {
                throw e;
            }
        }

        if (value == null && defaultValue != null) {
            return;
        }

        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setObject(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return readValue(jsonReader);
    }

    /**
     * Read the specific value type from JSONReader.
     * Subclasses must implement this to call the appropriate reader method.
     *
     * @param jsonReader the JSON reader
     * @return the read value
     */
    protected abstract V readValue(JSONReader jsonReader);
}
