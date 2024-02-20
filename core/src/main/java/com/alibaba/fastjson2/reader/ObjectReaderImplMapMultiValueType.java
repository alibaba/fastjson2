package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.MapMultiValueType;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectReaderImplMapMultiValueType
        implements ObjectReader {
    final Class mapType;
    final Class instanceType;
    final Function builder;
    final MapMultiValueType multiValueType;

    public ObjectReaderImplMapMultiValueType(MapMultiValueType multiValueType) {
        this.multiValueType = multiValueType;
        mapType = multiValueType.mapType;

        String mapClassName = mapType.getName();

        Class instanceType = mapType;
        Function builder = null;
        if (mapType == Map.class
                || mapType == AbstractMap.class
                || mapClassName.equals("java.util.Collections$SingletonMap")
        ) {
            instanceType = HashMap.class;
        } else if (mapClassName.equals("java.util.Collections$UnmodifiableMap")) {
            instanceType = LinkedHashMap.class;
        } else if (mapType == SortedMap.class) {
            instanceType = TreeMap.class;
        } else if (mapType == ConcurrentMap.class) {
            instanceType = ConcurrentHashMap.class;
        } else if (mapType == ConcurrentNavigableMap.class) {
            instanceType = ConcurrentSkipListMap.class;
        }
        this.instanceType = instanceType;
        this.builder = null;
    }

    @Override
    public Object createInstance(long features) {
        if (instanceType != null && !instanceType.isInterface()) {
            try {
                return instanceType.newInstance();
            } catch (Exception e) {
                throw new JSONException("create map error", e);
            }
        }
        return new HashMap();
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!jsonReader.nextIfObjectStart()) {
            if (jsonReader.nextIfNullOrEmptyString()) {
                return null;
            }
            throw new JSONException(jsonReader.info("expect '{', but '" + jsonReader.current() + "'"));
        }

        JSONReader.Context context = jsonReader.getContext();
        long contextFeatures = context.features | features;
        Map object, innerMap = null;
        if (instanceType == HashMap.class) {
            Supplier<Map> objectSupplier = context.getObjectSupplier();
            if (mapType == Map.class && objectSupplier != null) {
                object = objectSupplier.get();
                innerMap = TypeUtils.getInnerMap(object);
            } else {
                object = new HashMap<>();
            }
        } else if (instanceType == JSONObject.class) {
            object = new JSONObject();
        } else {
            object = (Map) createInstance(contextFeatures);
        }

        String name;
        Type valueType = null;
        for (int i = 0; ; i++) {
            if (jsonReader.nextIfObjectEnd() || jsonReader.isEnd()) {
                break;
            }

            if (jsonReader.nextIfNull()) {
                if (!jsonReader.nextIfMatch(':')) {
                    throw new JSONException(jsonReader.info("illegal json"));
                }
                name = null;
            } else {
                name = jsonReader.readFieldName();
                valueType = multiValueType.getType(name);
            }

            Object value;
            if (valueType == null) {
                value = jsonReader.readAny();
            } else {
                ObjectReader valueObjectReader = jsonReader.getObjectReader(valueType);
                value = valueObjectReader.readObject(jsonReader, valueType, fieldName, 0);
            }

            if (value == null && (contextFeatures & JSONReader.Feature.IgnoreNullPropertyValue.mask) != 0) {
                continue;
            }

            Object origin;
            if (innerMap != null) {
                origin = innerMap.put(name, value);
            } else {
                origin = object.put(name, value);
            }

            if (origin != null) {
                if ((contextFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
                    if (origin instanceof Collection) {
                        ((Collection) origin).add(value);
                        object.put(name, origin);
                    } else {
                        JSONArray array = JSONArray.of(origin, value);
                        object.put(name, array);
                    }
                }
            }
        }

        jsonReader.nextIfComma();

        if (builder != null) {
            return builder.apply(object);
        }

        return object;
    }
}
