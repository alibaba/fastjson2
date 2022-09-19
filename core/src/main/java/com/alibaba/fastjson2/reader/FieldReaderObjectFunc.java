package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.function.BiConsumer;

final class FieldReaderObjectFunc<T, V>
        extends FieldReaderImpl<T>
        implements FieldReaderObject<T, V> {
    final BiConsumer<T, V> function;
    protected ObjectReader fieldObjectReader;

    FieldReaderObjectFunc(
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
            BiConsumer<T, V> function,
            ObjectReader fieldObjectReader
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, null);
        this.function = function;
        this.fieldObjectReader = fieldObjectReader;
    }

    @Override
    public ObjectReader getInitReader() {
        return fieldObjectReader;
    }

    @Override
    public void accept(T object, Object value) {
        if (fieldType == Float.class) {
            value = TypeUtils.toFloat(value);
        } else if (fieldType == Double.class) {
            value = TypeUtils.toDouble(value);
        }

        if (value == null) {
            if (fieldClass == StackTraceElement[].class) {
                return;
            }
        }

        if (schema != null) {
            schema.assertValidate(value);
        }

        function.accept(object, (V) value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        ObjectReader objectReader;
        if (this.fieldObjectReader != null) {
            objectReader = this.fieldObjectReader;
        } else {
            objectReader = this.fieldObjectReader = jsonReader.getContext().getObjectReader(fieldType);
        }

        if (jsonReader.isReference()) {
            String reference = jsonReader.readReference();
            if ("..".equals(reference)) {
                accept(object, object);
            } else {
                addResolveTask(jsonReader, object, reference);
            }
            return;
        }

        Object value = jsonReader.isJSONB()
                ? objectReader.readJSONBObject(jsonReader, fieldType, fieldName, features)
                : objectReader.readObject(jsonReader, fieldType, fieldName, features);
        accept(object, value);
    }

    @Override
    public ObjectReader getFieldObjectReader(JSONReader.Context context) {
        if (fieldObjectReader == null) {
            fieldObjectReader = context
                    .getObjectReader(fieldType);
        }
        return fieldObjectReader;
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        ObjectReader objectReader;
        if (this.fieldObjectReader != null) {
            objectReader = this.fieldObjectReader;
        } else {
            objectReader = this.fieldObjectReader = jsonReader.getContext().getObjectReader(fieldType);
        }

        return jsonReader.isJSONB()
                ? objectReader.readJSONBObject(jsonReader, fieldType, fieldName, features)
                : objectReader.readObject(jsonReader, fieldType, fieldName, features);
    }
}
