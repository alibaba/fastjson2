package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

class FieldReaderMapMethodReadOnly<T> extends FieldReaderObjectMethod<T> implements FieldReaderReadOnly<T> {
    volatile ObjectReader itemReader;

    FieldReaderMapMethodReadOnly(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, method);
    }

    @Override
    public ObjectReader getItemObjectReader(JSONReader jsonReader) {
        if (itemReader != null) {
            return itemReader;
        }

        ObjectReader objectReader = getObjectReader(jsonReader);
        if (objectReader instanceof ObjectReaderImplMap) {
            return itemReader = ObjectReaderImplString.INSTANCE;
        }

        if (objectReader instanceof ObjectReaderImplMapTyped) {
            Type valueType = ((ObjectReaderImplMapTyped) objectReader).valueType;
            return itemReader = jsonReader.getObjectReader(valueType);
        }

        return ObjectReaderImplObject.INSTANCE;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        Map map;
        try {
            map = (Map) method.invoke(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }

        if (map == Collections.EMPTY_MAP) {
            return;
        }

        String name = map.getClass().getName();
        if ("java.util.Collections$UnmodifiableMap".equals(name)) {
            return;
        }

        if (schema != null) {
            schema.validate(value);
        }

        map.putAll((Map) value);
    }

    @Override
    public void processExtra(JSONReader jsonReader, Object object) {
        Map map;
        try {
            map = (Map) method.invoke(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }

        String name = jsonReader.getFieldName();

        ObjectReader itemObjectReader = getItemObjectReader(jsonReader);
        Object value = itemObjectReader.readObject(jsonReader, 0);
        map.put(name, value);
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
