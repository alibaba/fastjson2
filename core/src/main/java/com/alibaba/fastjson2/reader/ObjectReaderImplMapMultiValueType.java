package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.GuavaSupport;
import com.alibaba.fastjson2.util.MapMultiValueType;
import com.alibaba.fastjson2.util.ReferenceKey;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.reader.ObjectReaderImplMap.*;

public class ObjectReaderImplMapMultiValueType
        implements ObjectReader {
    final Class mapType;
    final Class instanceType;
    final Function builder;
    final MapMultiValueType multiValueType;

    public ObjectReaderImplMapMultiValueType(MapMultiValueType multiValueType) {
        this.multiValueType = multiValueType;
        mapType = multiValueType.getMapType();

        Class instanceType = mapType;
        Function builder = null;
        if (mapType == Map.class
                || mapType == AbstractMap.class
                || mapType == CLASS_SINGLETON_MAP
        ) {
            instanceType = HashMap.class;
        } else if (mapType == CLASS_UNMODIFIABLE_MAP) {
            instanceType = LinkedHashMap.class;
        } else if (mapType == SortedMap.class
                || mapType == CLASS_UNMODIFIABLE_SORTED_MAP
                || mapType == CLASS_UNMODIFIABLE_NAVIGABLE_MAP
        ) {
            instanceType = TreeMap.class;
        } else if (mapType == ConcurrentMap.class) {
            instanceType = ConcurrentHashMap.class;
        } else if (mapType == ConcurrentNavigableMap.class) {
            instanceType = ConcurrentSkipListMap.class;
        } else {
            switch (mapType.getTypeName()) {
                case "com.google.common.collect.ImmutableMap":
                case "com.google.common.collect.RegularImmutableMap":
                    instanceType = HashMap.class;
                    builder = GuavaSupport.immutableMapConverter();
                    break;
                case "com.google.common.collect.SingletonImmutableBiMap":
                    instanceType = HashMap.class;
                    builder = GuavaSupport.singletonBiMapConverter();
                    break;
                case "java.util.Collections$SynchronizedMap":
                    instanceType = HashMap.class;
                    builder = (Function<Map, Map>) Collections::synchronizedMap;
                    break;
                case "java.util.Collections$SynchronizedNavigableMap":
                    instanceType = TreeMap.class;
                    builder = (Function<NavigableMap, NavigableMap>) Collections::synchronizedNavigableMap;
                    break;
                case "java.util.Collections$SynchronizedSortedMap":
                    instanceType = TreeMap.class;
                    builder = (Function<SortedMap, SortedMap>) Collections::synchronizedSortedMap;
                    break;
                default:
                    break;
            }
        }
        this.instanceType = instanceType;
        this.builder = builder;
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
        long contextFeatures = context.getFeatures() | features;
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

        jsonReader.nextIfMatch(',');

        if (builder != null) {
            return builder.apply(object);
        }

        return object;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName1, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        jsonReader.nextIfMatch(BC_OBJECT);

        JSONReader.Context context = jsonReader.getContext();
        long contextFeatures = context.getFeatures() | features;
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

        for (int i = 0; ; i++) {
            byte type = jsonReader.getType();
            if (type == BC_OBJECT_END) {
                jsonReader.next();
                break;
            }

            Object name;
            if (type >= BC_STR_ASCII_FIX_MIN) {
                name = jsonReader.readFieldName();
            } else if (jsonReader.nextIfMatch(BC_REFERENCE)) {
                String reference = jsonReader.readString();
                name = new ReferenceKey(i);
                jsonReader.addResolveTask(object, name, JSONPath.of(reference));
            } else {
                name = jsonReader.readAny();
            }

            Type valueType = name instanceof String ? multiValueType.getType((String) name) : null;
            Object value;
            if (jsonReader.isReference()) {
                String reference = jsonReader.readReference();
                if ("..".equals(reference)) {
                    value = object;
                } else {
                    value = null;
                    jsonReader.addResolveTask(object, name, JSONPath.of(reference));
                }
            } else if (valueType == null) {
                value = jsonReader.readAny();
            } else {
                ObjectReader valueObjectReader = jsonReader.getObjectReader(valueType);
                value = valueObjectReader.readJSONBObject(jsonReader, valueType, name, 0);
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

            if (origin != null && (contextFeatures & JSONReader.Feature.DuplicateKeyValueAsArray.mask) != 0) {
                if (origin instanceof Collection) {
                    ((Collection) origin).add(value);
                    object.put(name, origin);
                } else {
                    JSONArray array = JSONArray.of(origin, value);
                    object.put(name, array);
                }
            }
        }

        if (builder != null) {
            return builder.apply(object);
        }

        return object;
    }
}
