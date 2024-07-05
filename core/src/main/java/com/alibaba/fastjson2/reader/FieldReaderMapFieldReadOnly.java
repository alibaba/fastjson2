package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

class FieldReaderMapFieldReadOnly<T>
        extends FieldReaderMapField<T> {
    FieldReaderMapFieldReadOnly(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            int ordinal,
            long features,
            String format,
            JSONSchema schema,
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
                schema,
                field,
                arrayToMapKey,
                arrayToMapDuplicateHandler);
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
            map = (Map) field.get(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }

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
        Map map;
        try {
            map = (Map) field.get(object);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }

        String name = jsonReader.getFieldName();

        ObjectReader itemObjectReader = getItemObjectReader(jsonReader);
        Object value = itemObjectReader.readObject(jsonReader, null, name, 0);
        map.put(name, value);
    }

    @Override
    public void acceptExtra(Object object, String name, Object value) {
        Map map;
        try {
            map = (Map) field.get(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error");
        }

        map.put(name, value);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (arrayToMapKey != null && jsonReader.isArray()) {
            Map map;
            try {
                map = (Map) field.get(object);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error");
            }
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
            value = initReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        } else {
            value = initReader.readObject(jsonReader, fieldType, fieldName, features);
        }

        accept(object, value);
    }

    protected void acceptAny(T object, Object fieldValue, long features) {
        if (arrayToMapKey != null && fieldValue instanceof Collection) {
            Map map;
            try {
                map = (Map) field.get(object);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error");
            }

            arrayToMap(map,
                    (Collection) fieldValue,
                    arrayToMapKey,
                    JSONFactory.getObjectReader(valueType, this.features | features),
                    arrayToMapDuplicateHandler);
            return;
        }

        super.acceptAny(object, fieldValue, features);
    }
}
