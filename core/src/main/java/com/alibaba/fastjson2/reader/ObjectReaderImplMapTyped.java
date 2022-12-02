package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.ReferenceKey;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.*;

class ObjectReaderImplMapTyped
        implements ObjectReader {
    final Class mapType;
    final Class instanceType;
    final Type keyType;
    final Type valueType;
    final Class valueClass;
    final long features;
    final Function builder;

    final Constructor defaultConstructor;

    ObjectReader valueObjectReader;
    ObjectReader keyObjectReader;

    public ObjectReaderImplMapTyped(Class mapType, Class instanceType, Type keyType, Type valueType, long features, Function builder) {
        if (keyType == Object.class) {
            keyType = null;
        }

        this.mapType = mapType;
        this.instanceType = instanceType;
        this.keyType = keyType;
        this.valueType = valueType;
        this.valueClass = TypeUtils.getClass(valueType);
        this.features = features;
        this.builder = builder;

        Constructor defaultConstructor = null;
        Constructor[] constructors = this.instanceType.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getParameterCount() == 0
                    && !Modifier.isPublic(constructor.getModifiers())) {
                constructor.setAccessible(true);
                defaultConstructor = constructor;
                break;
            }
        }
        this.defaultConstructor = defaultConstructor;
    }

    @Override
    public Class getObjectClass() {
        return mapType;
    }

    @Override
    public Object createInstance(Map input, long features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        Map<String, Object> object = (Map<String, Object>) createInstance();
        for (Iterator<Map.Entry> it = input.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = it.next();
            String fieldName = entry.getKey().toString();
            Object fieldValue = entry.getValue();

            Object value = fieldValue;
            Class<?> valueClass = value.getClass();
            Function typeConvert = provider.getTypeConvert(valueClass, valueType);
            if (typeConvert != null) {
                value = typeConvert.apply(value);
            } else if (value instanceof Map) {
                Map map = (Map) value;
                if (valueObjectReader == null) {
                    valueObjectReader = provider.getObjectReader(valueType);
                }
                value = valueObjectReader.createInstance(map, features);
            } else if (value instanceof Collection) {
                if (valueObjectReader == null) {
                    valueObjectReader = provider.getObjectReader(valueType);
                }
                value = valueObjectReader.createInstance((Collection) value);
            } else {
                if (!valueClass.isInstance(value)) {
                    throw new JSONException("can not convert from " + valueClass + " to " + valueType);
                }
            }
            object.put(fieldName, value);
        }

        if (builder != null) {
            return builder.apply(object);
        }

        return object;
    }

    @Override
    public Object createInstance(long features) {
        if (instanceType != null && !instanceType.isInterface()) {
            try {
                if (defaultConstructor != null) {
                    return defaultConstructor.newInstance();
                }
                return instanceType.newInstance();
            } catch (Exception e) {
                throw new JSONException("create map error", e);
            }
        }
        return new HashMap();
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        ObjectReader objectReader = null;
        Function builder = this.builder;
        if (jsonReader.getType() == BC_TYPED_ANY) {
            objectReader = jsonReader.checkAutoType(mapType, 0, this.features | features);

            if (objectReader != null && objectReader != this) {
                builder = objectReader.getBuildFunction();
                if (!(objectReader instanceof ObjectReaderImplMap) && !(objectReader instanceof ObjectReaderImplMapTyped)) {
                    return objectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
                }
            }
        }

        byte firstType = jsonReader.getType();
        if (firstType == BC_NULL) {
            jsonReader.next();
            return null;
        }

        if (firstType == BC_OBJECT) {
            jsonReader.next();
        }

        JSONReader.Context context = jsonReader.getContext();
        long contextFeatures = features | context.getFeatures();

        Map object;
        if (objectReader != null) {
            object = (Map) objectReader.createInstance(contextFeatures);
        } else {
            object = instanceType == HashMap.class
                    ? new HashMap<>()
                    : (Map) createInstance();
        }

        for (int i = 0; ; ++i) {
            byte type = jsonReader.getType();
            if (type == BC_OBJECT_END) {
                jsonReader.next();
                break;
            }

            Object name;
            if (keyType == String.class || jsonReader.isString()) {
                name = jsonReader.readFieldName();
            } else {
                if (jsonReader.isReference()) {
                    String reference = jsonReader.readReference();
                    name = new ReferenceKey(i);
                    jsonReader.addResolveTask(object, name, JSONPath.of(reference));
                } else {
                    if (keyObjectReader == null && keyType != null) {
                        keyObjectReader = jsonReader.getObjectReader(keyType);
                    }

                    if (keyObjectReader == null) {
                        name = jsonReader.readAny();
                    } else {
                        name = keyObjectReader.readJSONBObject(jsonReader, null, null, features);
                    }
                }
            }

            if (jsonReader.isReference()) {
                String reference = jsonReader.readReference();
                if ("..".equals(reference)) {
                    object.put(name, object);
                } else {
                    jsonReader.addResolveTask((Map) object, name, JSONPath.of(reference));
                    if (!(object instanceof ConcurrentMap)) {
                        object.put(name, null);
                    }
                }
                continue;
            }

            if (jsonReader.nextIfNull()) {
                object.put(name, null);
                continue;
            }

            Object value;
            if (valueType == Object.class) {
                value = jsonReader.readAny();
            } else {
                ObjectReader autoTypeValueReader = jsonReader.checkAutoType(valueClass, 0, features);
                if (autoTypeValueReader != null) {
                    value = autoTypeValueReader.readJSONBObject(jsonReader, valueType, name, features);
                } else {
                    if (valueObjectReader == null) {
                        valueObjectReader = jsonReader.getObjectReader(valueType);
                    }
                    value = valueObjectReader.readJSONBObject(jsonReader, valueType, name, features);
                }
            }
            object.put(name, value);
        }

        if (builder != null) {
            return builder.apply(object);
        }

        return object;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        boolean match = jsonReader.nextIfMatch('{');
        if (!match) {
            if (jsonReader.nextIfNull()) {
                return null;
            }

            throw new JSONException(jsonReader.info("expect '{', but '['"));
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
        } else {
            object = (Map) createInstance(contextFeatures);
        }

        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfMatch('}') || jsonReader.isEnd()) {
                break;
            }

            Object name;
            if (jsonReader.nextIfNull()) {
                if (!jsonReader.nextIfMatch(':')) {
                    throw new JSONException(jsonReader.info("illegal json"));
                }
                name = null;
            } else if (keyType == String.class) {
                name = jsonReader.readFieldName();
                if (i == 0
                        && (contextFeatures & JSONReader.Feature.SupportAutoType.mask) != 0
                        && name.equals(getTypeKey())
                ) {
                    long typeHashCode = jsonReader.readTypeHashCode();
                    ObjectReader objectReaderAutoType = context.getObjectReaderAutoType(typeHashCode);
                    if (objectReaderAutoType == null) {
                        String typeName = jsonReader.getString();
                        objectReaderAutoType = context.getObjectReaderAutoType(typeName, mapType, features);
                    }
                    if (objectReaderAutoType != null) {
                        if (objectReaderAutoType instanceof ObjectReaderImplMap) {
                            if (!object.getClass().equals(((ObjectReaderImplMap) objectReaderAutoType).instanceType)) {
                                object = (Map) objectReaderAutoType.createInstance(features);
                            }
                        }
                    }
                    continue;
                }

                if (name == null) {
                    name = jsonReader.readString();
                    if (!jsonReader.nextIfMatch(':')) {
                        throw new JSONException(jsonReader.info("illegal json"));
                    }
                }
            } else {
                if (keyObjectReader != null) {
                    name = keyObjectReader.readObject(jsonReader, null, null, 0);
                } else {
                    name = jsonReader.read(keyType);
                }
                if (i == 0
                        && (contextFeatures & JSONReader.Feature.SupportAutoType.mask) != 0
                        && name.equals(getTypeKey())) {
                    continue;
                }
                jsonReader.nextIfMatch(':');
            }
            if (valueObjectReader == null) {
                valueObjectReader = jsonReader.getObjectReader(valueType);
            }
            Object value = valueObjectReader.readObject(jsonReader, fieldType, fieldName, 0);
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
                        object.put(name, value);
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
}
