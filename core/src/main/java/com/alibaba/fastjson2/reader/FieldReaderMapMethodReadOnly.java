package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.BiConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class FieldReaderMapMethodReadOnly<T>
        extends FieldReaderMapMethod<T> {
    FieldReaderMapMethodReadOnly(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            Method method,
            Field field,
            String arrayToMapKey,
            BiConsumer arrayToMapDuplicateHandler
    ) {
        super(
                fieldName,
                fieldType,
                fieldClass,
                ordinal,
                features,
                format,
                null,
                null,
                method,
                field,
                null,
                arrayToMapKey,
                arrayToMapDuplicateHandler
        );
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

        Map map = getReadOnlyMap(object);
        if (map == Collections.EMPTY_MAP || map == null) {
            return;
        }

        String name = map.getClass().getName();
        if ("java.util.Collections$UnmodifiableMap".equals(name)) {
            return;
        }
        map.putAll((Map) value);
    }

    @Override
    public void processExtra(JSONReader jsonReader, Object object) {
        String name = jsonReader.getFieldName();

        ObjectReader itemObjectReader = getItemObjectReader(jsonReader);
        Object value = itemObjectReader.readObject(jsonReader, getItemType(), fieldName, 0);
        getReadOnlyMap(object)
                .put(name, value);
    }

    public void acceptExtra(Object object, String name, Object value) {
        getReadOnlyMap(object)
                .put(name, value);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (arrayToMapKey != null && jsonReader.isArray()) {
            Map map = getReadOnlyMap(object);
            List array = jsonReader.readArray(valueType);
            arrayToMap(map,
                    array,
                    arrayToMapKey,
                    JSONFactory.getObjectReader(valueType, this.features | features),
                    arrayToMapDuplicateHandler);
            return;
        }

        if (initReader == null) {
            initReader = jsonReader
                    .getContext()
                    .getObjectReader(fieldType);
        }

        Object value;
        if (jsonReader.jsonb) {
            value = initReader.readJSONBObject(jsonReader, getItemType(), fieldName, features);
        } else {
            value = initReader.readObject(jsonReader, getItemType(), fieldName, features);
        }

        accept(object, value);
    }

    protected void acceptAny(T object, Object fieldValue, long features) {
        if (arrayToMapKey != null && fieldValue instanceof Collection) {
            Map map = getReadOnlyMap(object);

            arrayToMap(map,
                    (Collection) fieldValue,
                    arrayToMapKey,
                    JSONFactory.getObjectReader(valueType, this.features | features),
                    arrayToMapDuplicateHandler);
            return;
        }

        super.acceptAny(object, fieldValue, features);
    }

    private Map getReadOnlyMap(Object object) {
        Map map;
        try {
            map = (Map) method.invoke(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error");
        }
        return map;
    }
}
