package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

class FieldReaderCollectionMethodReadOnly<T>
        extends FieldReaderObject<T> {
    FieldReaderCollectionMethodReadOnly(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            JSONSchema schema,
            Method setter,
            Field field
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, null, schema, setter, field, null);
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

        if (collection == Collections.EMPTY_LIST || collection == Collections.EMPTY_SET || collection == null) {
            if (schema != null) {
                schema.assertValidate(collection);
            }

            return;
        }

        String name = collection.getClass().getName();
        if ("java.util.Collections$UnmodifiableRandomAccessList".equals(name)
                || "java.util.Arrays$ArrayList".equals(name)
                || "java.util.Collections$SingletonList".equals(name)
                || name.startsWith("java.util.ImmutableCollections$")) {
            return;
        }

        if (value == collection) {
            return;
        }

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
                    item = itemReader.createInstance((Map) item, 0L);
                }
            }
            collection.add(item);
        }

        if (schema != null) {
            schema.assertValidate(collection);
        }
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
        Object value = jsonReader.isJSONB()
                ? initReader.readJSONBObject(jsonReader, fieldType, fieldName, 0)
                : initReader.readObject(jsonReader, fieldType, fieldName, 0);
        accept(object, value);
    }
}
