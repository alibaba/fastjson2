package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.*;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public final class FieldReaderAtomicReference<T, V>
        extends FieldReaderObject<T> {
    final Type referenceType;

    public FieldReaderAtomicReference(
            String fieldName,
            Type fieldType,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Object defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer<T, V> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter, null);

        Type referenceType = null;
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) fieldType;
            Type[] arguments = paramType.getActualTypeArguments();
            if (arguments.length == 1) {
                referenceType = arguments[0];
            }
        }
        this.referenceType = referenceType;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (jsonReader.nextIfNull()) {
            return;
        }

        Object refValue = jsonReader.read(referenceType);
        accept(object, refValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        return jsonReader.read(referenceType);
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        if (isParameter() || (value == null && (features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0)) {
            return;
        }

        if (isReadOnly()) {
            try {
                AtomicReference<V> atomicReference = (AtomicReference<V>) propertyAccessor.getObject(object);
                if (atomicReference != null) {
                    atomicReference.set((V) value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to access AtomicReference field/method: " + fieldName, e);
            }
        } else {
            // For non-read-only cases, we can replace the AtomicReference entirely
            AtomicReference<V> newAtomicReference = new AtomicReference<>((V) value);
            propertyAccessor.setObject(object, newAtomicReference);
        }
    }

    @Override
    public boolean isReadOnly() {
        return method != null && method.getParameterCount() == 0 ||
               (field != null && Modifier.isFinal(field.getModifiers()));
    }
}
