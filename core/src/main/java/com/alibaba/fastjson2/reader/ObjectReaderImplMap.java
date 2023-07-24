package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;
import static com.alibaba.fastjson2.util.TypeUtils.CLASS_JSON_OBJECT_1x;

public final class ObjectReaderImplMap
        implements ObjectReader {
    static Function UNSAFE_OBJECT_CREATOR;
    static final Class CLASS_SINGLETON_MAP = Collections.singletonMap(1, 1).getClass();
    static final Class CLASS_EMPTY_MAP = Collections.EMPTY_MAP.getClass();
    static final Class CLASS_EMPTY_SORTED_MAP = Collections.emptySortedMap().getClass();
    static final Class CLASS_EMPTY_NAVIGABLE_MAP = Collections.emptyNavigableMap().getClass();
    static final Class CLASS_UNMODIFIABLE_MAP = Collections.unmodifiableMap(Collections.emptyMap()).getClass();
    static final Class CLASS_UNMODIFIABLE_SORTED_MAP = Collections.unmodifiableSortedMap(Collections.emptySortedMap()).getClass();
    static final Class CLASS_UNMODIFIABLE_NAVIGABLE_MAP = Collections.unmodifiableNavigableMap(Collections.emptyNavigableMap()).getClass();

    public static final ObjectReaderImplMap INSTANCE = new ObjectReaderImplMap(null, HashMap.class, HashMap.class, 0, null);
    public static final ObjectReaderImplMap INSTANCE_OBJECT = new ObjectReaderImplMap(null, JSONObject.class, JSONObject.class, 0, null);

    final Type fieldType;
    final Class mapType;
    final long mapTypeHash;
    final Class instanceType;
    final long features;
    final Function builder;
    Object mapSingleton;
    volatile boolean instanceError;

    public static ObjectReader of(Type fieldType, Class mapType, long features) {
        Function builder = null;
        Class instanceType = mapType;

        if ("".equals(instanceType.getSimpleName())) {
            instanceType = mapType.getSuperclass();
            if (fieldType == null) {
                fieldType = mapType.getGenericSuperclass();
            }
        }

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

        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;

            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length == 2 && !"org.springframework.util.LinkedMultiValueMap".equals(instanceType.getName())) {
                Type keyType = actualTypeArguments[0];
                Type valueType = actualTypeArguments[1];

                if (keyType == String.class && valueType == String.class && builder == null) {
                    return new ObjectReaderImplMapString(mapType, instanceType, features);
                }

                return new ObjectReaderImplMapTyped(mapType, instanceType, keyType, valueType, 0, builder);
            }
        }

        if (fieldType == null && features == 0) {
            if (mapType == HashMap.class && instanceType == HashMap.class) {
                return INSTANCE;
            }

            if (mapType == JSONObject.class && instanceType == JSONObject.class) {
                return INSTANCE_OBJECT;
            }
        }

        String instanceTypeName = instanceType.getName();
        switch (instanceTypeName) {
            case "com.alibaba.fastjson.JSONObject":
                builder = createObjectSupplier(instanceType);
                instanceType = HashMap.class;
                break;
            case "com.google.common.collect.RegularImmutableMap":
                builder = GuavaSupport.immutableMapConverter();
                instanceType = HashMap.class;
                break;
            case "com.google.common.collect.SingletonImmutableBiMap":
                builder = GuavaSupport.singletonBiMapConverter();
                instanceType = HashMap.class;
                break;
            case "com.google.common.collect.ArrayListMultimap":
                builder = GuavaSupport.createConvertFunction(instanceType);
                instanceType = HashMap.class;
                break;
            case "kotlin.collections.EmptyMap": {
                Object mapSingleton;
                try {
                    Field field = instanceType.getField("INSTANCE");
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    mapSingleton = field.get(null);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new IllegalStateException("Failed to get singleton of " + instanceType, e);
                }
                return new ObjectReaderImplMap(instanceType, features, mapSingleton);
            }
            case "java.util.Collections$EmptyMap": {
                return new ObjectReaderImplMap(instanceType, features, Collections.EMPTY_MAP);
            }
            default:
                if (instanceType == JSONObject1O.class) {
                    builder = createObjectSupplier(CLASS_JSON_OBJECT_1x);
                    instanceType = LinkedHashMap.class;
                } else if (mapType == CLASS_UNMODIFIABLE_MAP) {
                    builder = (Function<Map, Map>) Collections::unmodifiableMap;
                } else if (mapType == CLASS_UNMODIFIABLE_SORTED_MAP) {
                    builder = (Function<SortedMap, SortedMap>) Collections::unmodifiableSortedMap;
                } else if (mapType == CLASS_UNMODIFIABLE_NAVIGABLE_MAP) {
                    builder = (Function<NavigableMap, NavigableMap>) Collections::unmodifiableNavigableMap;
                } else if (mapType == CLASS_SINGLETON_MAP) {
                    builder = (Function<Map, Map>) (Map map) -> {
                        Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
                        return Collections.singletonMap(entry.getKey(), entry.getValue());
                    };
                } else if (mapType == EnumMap.class) {
                    instanceType = LinkedHashMap.class;
                    builder = e -> new EnumMap((Map) e);
                }
        }

        return new ObjectReaderImplMap(fieldType, mapType, instanceType, features, builder);
    }

    ObjectReaderImplMap(Class mapClass, long features, Object mapSingleton) {
        this(mapClass, mapClass, mapClass, features, null);
        this.mapSingleton = mapSingleton;
    }

    ObjectReaderImplMap(Type fieldType, Class mapType, Class instanceType, long features, Function builder) {
        this.fieldType = fieldType;
        this.mapType = mapType;
        this.mapTypeHash = Fnv.hashCode64(TypeUtils.getTypeName(mapType));
        this.instanceType = instanceType;
        this.features = features;
        this.builder = builder;
    }

    @Override
    public Class getObjectClass() {
        return mapType;
    }

    @Override
    public Function getBuildFunction() {
        return builder;
    }

    @Override
    public Object createInstance(long features) {
        if (instanceType == HashMap.class) {
            return new HashMap<>();
        }

        if (instanceType == LinkedHashMap.class) {
            return new LinkedHashMap<>();
        }

        if (instanceType == JSONObject.class) {
            return new JSONObject();
        }

        if (mapSingleton != null) {
            return mapSingleton;
        }

        if (instanceType == CLASS_EMPTY_SORTED_MAP) {
            return Collections.emptySortedMap();
        }

        if (instanceType == CLASS_EMPTY_NAVIGABLE_MAP) {
            return Collections.emptyNavigableMap();
        }

        String instanceTypeName = instanceType.getName();
        switch (instanceTypeName) {
            case "com.ali.com.google.common.collect.EmptyImmutableBiMap":
                return ((Supplier) () -> {
                    try {
                        return UNSAFE.allocateInstance(instanceType);
                    } catch (InstantiationException e) {
                        throw new JSONException("create map error : " + instanceType);
                    }
                }).get();
            case "java.util.ImmutableCollections$Map1":
                return new HashMap<>();
            case "java.util.ImmutableCollections$MapN":
                return new LinkedHashMap<>();
            default:
                break;
        }

        try {
            return instanceType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new JSONException("create map error : " + instanceType);
        }
    }

    @Override
    public Object createInstance(Map map, long features) {
        if (mapType.isInstance(map)) {
            return map;
        }

        if (mapType == JSONObject.class) {
            return new JSONObject(map);
        }

        Map instance = (Map) this.createInstance(features);
        instance.putAll(map);

        if (builder != null) {
            return builder.apply(instance);
        }

        return instance;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName1, long features) {
        ObjectReader objectReader = jsonReader.checkAutoType(mapType, mapTypeHash, this.features | features);
        if (objectReader != null && objectReader != this) {
            return objectReader.readJSONBObject(jsonReader, fieldType, fieldName1, features);
        }

        if (jsonReader.nextIfNull()) {
            return null;
        }

        boolean emptyObject = false;
        jsonReader.nextIfMatch(BC_OBJECT);

        Supplier<Map> objectSupplier = jsonReader.getContext().getObjectSupplier();
        Map map = null;
        if (mapType == null && objectSupplier != null) {
            map = objectSupplier.get();
        } else {
            if (instanceType == HashMap.class) {
                map = new HashMap<>();
            } else if (instanceType == LinkedHashMap.class) {
                map = new LinkedHashMap<>();
            } else if (instanceType == JSONObject.class) {
                map = new JSONObject();
            } else if (instanceType == CLASS_EMPTY_MAP) {
                map = Collections.EMPTY_MAP;
            } else {
                JSONException error = null;

                if (!instanceError) {
                    try {
                        map = (Map) instanceType.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        instanceError = true;
                        error = new JSONException(jsonReader.info("create map error " + instanceType));
                    }
                }

                if (instanceError && Map.class.isAssignableFrom(instanceType.getSuperclass())) {
                    try {
                        map = (Map) instanceType.getSuperclass().newInstance();
                        error = null;
                    } catch (InstantiationException | IllegalAccessException e) {
                        if (error == null) {
                            error = new JSONException(jsonReader.info("create map error " + instanceType));
                        }
                    }
                }

                if (error != null) {
                    throw error;
                }
            }
        }

        if (!emptyObject) {
            for (int i = 0; ; ++i) {
                byte type = jsonReader.getType();
                if (type == BC_OBJECT_END) {
                    jsonReader.next();
                    break;
                }

                Object fieldName;
                if (type >= BC_STR_ASCII_FIX_MIN) {
                    fieldName = jsonReader.readFieldName();
                } else if (jsonReader.nextIfMatch(BC_REFERENCE)) {
                    String reference = jsonReader.readString();
                    fieldName = new ReferenceKey(i);
                    jsonReader.addResolveTask(map, fieldName, JSONPath.of(reference));
                } else {
                    fieldName = jsonReader.readAny();
                }

                if (jsonReader.isReference()) {
                    String reference = jsonReader.readReference();
                    if ("..".equals(reference)) {
                        map.put(fieldName, map);
                    } else {
                        jsonReader.addResolveTask(map, fieldName, JSONPath.of(reference));
                        map.put(fieldName, null);
                    }
                    continue;
                }

                Object value;
                type = jsonReader.getType();
                if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_UTF16BE) {
                    value = jsonReader.readString();
                } else if (type == BC_TYPED_ANY) {
                    ObjectReader autoTypeObjectReader = jsonReader.checkAutoType(Object.class, 0, this.features | features);
                    if (autoTypeObjectReader != null) {
                        value = autoTypeObjectReader.readJSONBObject(jsonReader, null, fieldName, features);
                    } else {
                        value = jsonReader.readAny();
                    }
                } else if (type == BC_TRUE) {
                    value = Boolean.TRUE;
                    jsonReader.next();
                } else if (type == BC_FALSE) {
                    value = Boolean.FALSE;
                    jsonReader.next();
                } else if (type == BC_REFERENCE) {
                    String reference = jsonReader.readReference();
                    if ("..".equals(reference)) {
                        value = map;
                    } else {
                        value = null;
                        jsonReader.addResolveTask(map, fieldName, JSONPath.of(reference));
                    }
                } else if (type == BC_OBJECT) {
                    value = jsonReader.readObject();
                } else if (type >= BC_ARRAY_FIX_MIN && type <= BC_ARRAY) {
                    value = jsonReader.readArray();
                } else {
                    value = jsonReader.readAny();
                }
                map.put(fieldName, value);
            }
        }

        if (builder != null) {
            return builder.apply(map);
        }
        return map;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.jsonb) {
            return readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        JSONReader.Context context = jsonReader.getContext();
        Supplier<Map> objectSupplier = jsonReader.getContext().getObjectSupplier();
        Map object;
        if (objectSupplier != null && (mapType == null || mapType == JSONObject.class || "com.alibaba.fastjson.JSONObject".equals(mapType.getName()))) {
            object = objectSupplier.get();
        } else {
            object = (Map) createInstance(context.getFeatures() | features);
        }

        jsonReader.read(object, features);

        jsonReader.nextIfComma();

        if (builder != null) {
            return builder.apply(object);
        }

        return object;
    }

    static Function createObjectSupplier(Class objectClass) {
        if (UNSAFE_OBJECT_CREATOR != null) {
            return UNSAFE_OBJECT_CREATOR;
        }
        return UNSAFE_OBJECT_CREATOR = new ObjectCreatorUF(objectClass);
    }

    static class ObjectCreatorUF
            implements Function {
        final Class objectClass;
        final Field map;
        final long mapOffset;

        ObjectCreatorUF(Class objectClass) {
            this.objectClass = objectClass;
            try {
                map = objectClass.getDeclaredField("map");
            } catch (NoSuchFieldException e) {
                throw new JSONException("field map not found", e);
            }
            mapOffset = UNSAFE.objectFieldOffset(map);
        }

        @Override
        public Object apply(Object map) {
            if (map == null) {
                map = new HashMap<>();
            }

            Object object;
            try {
                object = UNSAFE.allocateInstance(objectClass);
                UNSAFE.putObject(object, mapOffset, map);
            } catch (InstantiationException e) {
                throw new JSONException("create " + objectClass.getName() + " error", e);
            }
            return object;
        }
    }
}
