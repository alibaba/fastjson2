package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

class FielderReaderImplMapFieldReadOnly<T> extends FieldReaderObjectField<T> implements FieldReaderReadOnly<T> {
    FielderReaderImplMapFieldReadOnly(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Field field) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, field);
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        Map map;
        try {
            map = (Map) field.get(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }

        if (map == Collections.EMPTY_MAP) {
            return;
        }

        String name = map.getClass().getName();
        if (name.equals("java.util.Collections$UnmodifiableMap")) {
            return;
        }

        map.putAll((Map) value);
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

        Object value;
        if (jsonReader.isJSONB()) {
            value = fieldObjectReader.readJSONBObject(jsonReader, features);
        } else {
            value = fieldObjectReader.readObject(jsonReader, features);
        }

        accept(object, value);
    }
}
