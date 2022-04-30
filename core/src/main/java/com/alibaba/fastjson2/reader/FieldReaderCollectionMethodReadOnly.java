package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class FieldReaderCollectionMethodReadOnly<T> extends FieldReaderObjectMethod<T> implements FieldReaderReadOnly<T> {
    private final Type itemType;
    private ObjectReader itemReader;

    FieldReaderCollectionMethodReadOnly(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, setter);
        Type itemType = null;
        if (fieldType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) fieldType).getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                itemType = actualTypeArguments[0];
            }
        }
        this.itemType = itemType;
    }

    @Override
    public Type getItemType() {
        return itemType;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        Collection collection;
        try {
            collection = (Collection) method.invoke(object);
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

        if (value == collection) {
            return;
        }

        Type itemType = getItemType();
        Collection values = (Collection) value;
        for (Object item : values) {
            if (item == null) {
                collection.add(item);
                continue;
            }

            if (item instanceof Map && itemType instanceof Class) {
                if (!((Class) itemType).isAssignableFrom(item.getClass())) {
                    if (itemReader == null) {
                        itemReader = JSONFactory
                                .getDefaultObjectReaderProvider()
                                .getObjectReader(itemType);
                    }
                    item = itemReader.createInstance((Map) item);
                }
            }
            collection.add(item);
        }
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
        Object value = jsonReader.isJSONB()
                ? fieldObjectReader.readJSONBObject(jsonReader, 0)
                : fieldObjectReader.readObject(jsonReader, 0);
        accept(object, value);
    }
}
