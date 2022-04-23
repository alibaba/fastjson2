package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

final class FieldReaderCollectionFieldReadOnly<T> extends FieldReaderObjectField<T> implements FieldReaderReadOnly<T> {
    FieldReaderCollectionFieldReadOnly(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Field field) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, field);
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

        if (collection == Collections.EMPTY_LIST || collection == Collections.EMPTY_SET) {
            return;
        }

        String name = collection.getClass().getName();
        if (name.equals("java.util.Collections$UnmodifiableRandomAccessList")
                || name.equals("java.util.Collections$UnmodifiableRandomAccessList")) {
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
        if (fieldObjectReader == null) {
            fieldObjectReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }
        Object value = fieldObjectReader.readObject(jsonReader, 0);
        accept(object, value);
    }
}
