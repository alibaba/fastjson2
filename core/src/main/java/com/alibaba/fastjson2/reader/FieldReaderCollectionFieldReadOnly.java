package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

final class FieldReaderCollectionFieldReadOnly<T>
        extends FieldReaderObjectField<T> {
    FieldReaderCollectionFieldReadOnly(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, schema, field);
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        Collection collection;
        try {
            collection = (Collection) field.get(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }

        if (collection == Collections.EMPTY_LIST || collection == Collections.EMPTY_SET || collection == null) {
            return;
        }

        String name = collection.getClass().getName();
        if ("java.util.Collections$UnmodifiableRandomAccessList".equals(name)
                || "java.util.Arrays$ArrayList".equals(name)
                || "java.util.Collections$SingletonList".equals(name)
                || name.startsWith("java.util.ImmutableCollections$")) {
            return;
        }

        collection.addAll((Collection) value);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (initReader == null) {
            initReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }
        Object value = initReader.readObject(jsonReader, fieldType, fieldName, 0);
        accept(object, value);
    }
}
