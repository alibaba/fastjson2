package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

final class FieldReaderMapReadOnly<T, V>
        extends FieldReaderObject<T> {
    final BiConsumer<T, V> function;
    final String arrayToMapKey;
    final PropertyNamingStrategy namingStrategy;
    final Type valueType;
    final BiConsumer arrayToMapDuplicateHandler;
    ObjectReader itemReader;

    FieldReaderMapReadOnly(
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
            Field field,
            BiConsumer<T, V> function,
            String paramName,
            Parameter parameter,
            String arrayToMapKey,
            BiConsumer arrayToMapDuplicateHandler
    ) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter, null);
        this.function = function;
        this.valueType = TypeUtils.getMapValueType(fieldType);
        this.arrayToMapKey = arrayToMapKey;
        this.namingStrategy = PropertyNamingStrategy.of(format);
        this.arrayToMapDuplicateHandler = arrayToMapDuplicateHandler;
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

        Map map;
        try {
            map = getMap(object);
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

        if (schema != null) {
            schema.assertValidate(value);
        }

        map.putAll((Map) value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (arrayToMapKey != null && jsonReader.isArray()) {
            Map map;
            try {
                map = getMap(object);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }
            List array = jsonReader.readArray(valueType);
            FieldReaderObject.arrayToMap(map,
                    array,
                    arrayToMapKey,
                    namingStrategy,
                    JSONFactory.getObjectReader(valueType, features),
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

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
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

        return value;
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
    public void processExtra(JSONReader jsonReader, Object object) {
        String name = jsonReader.getFieldName();

        ObjectReader itemObjectReader = getItemObjectReader(jsonReader);
        Object value = itemObjectReader.readObject(jsonReader, getItemType(), fieldName, 0);

        Map map;
        try {
            map = getMap(object);
            map.put(name, value);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void acceptExtra(Object object, String name, Object value) {
        Map map;
        try {
            map = getMap(object);
            map.put(name, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error");
        }
    }

    protected void acceptAny(T object, Object fieldValue, long features) {
        if (arrayToMapKey != null && fieldValue instanceof Collection) {
            Map map;
            try {
                map = getMap(object);
            } catch (Exception e) {
                throw new JSONException("set " + fieldName + " error", e);
            }

            FieldReaderObject.arrayToMap(map,
                    (Collection) fieldValue,
                    arrayToMapKey,
                    namingStrategy,
                    JSONFactory.getObjectReader(valueType, this.features | features),
                    arrayToMapDuplicateHandler);
            return;
        }

        super.acceptAny(object, fieldValue, features);
    }

    private Map getMap(Object object) throws Exception {
        if (propertyAccessor != null) {
            return (Map) propertyAccessor.getObject(object);
        }
        return null;
    }
}
