package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

final class FieldReaderAtomicBoolean<T, V>
        extends FieldReader<T> {
    final BiConsumer<T, V> function;

    FieldReaderAtomicBoolean(
            String fieldName,
            Class<V> fieldClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            AtomicBoolean defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            BiConsumer<T, V> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, fieldClass, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
        this.function = function;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        try {
            Object fieldValue = null;
            if (field != null) {
                fieldValue = field.get(object);
            } else if (method != null) {
                fieldValue = method.invoke(object);
            } else if (function != null) {
                // For functional access, we handle it differently
                if (value instanceof AtomicBoolean) {
                    value = ((AtomicBoolean) value).get();
                }
                function.accept(object, (V) value);
                return;
            }

            if (fieldValue instanceof AtomicBoolean) {
                AtomicBoolean atomic = (AtomicBoolean) fieldValue;
                if (value instanceof AtomicBoolean) {
                    value = ((AtomicBoolean) value).get();
                }
                atomic.set((Boolean) value);
            }
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Boolean value = jsonReader.readBool();
        accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
