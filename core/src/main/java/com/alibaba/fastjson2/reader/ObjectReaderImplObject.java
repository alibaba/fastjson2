package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.*;

public final class ObjectReaderImplObject
        extends ObjectReaderPrimitive {
    public static final ObjectReaderImplObject INSTANCE = new ObjectReaderImplObject();

    public ObjectReaderImplObject() {
        super(Object.class);
    }

    @Override
    public Object createInstance(long features) {
        return new JSONObject();
    }

    public Object createInstance(Collection collection) {
        return collection;
    }

    @Override
    public Object createInstance(Map map, long features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Object typeKey = map.get(getTypeKey());

        if (typeKey instanceof String) {
            String typeName = (String) typeKey;
            long typeHash = Fnv.hashCode64(typeName);
            ObjectReader reader = null;
            if ((features & JSONReader.Feature.SupportAutoType.mask) != 0) {
                reader = autoType(provider, typeHash);
            }

            if (reader == null) {
                reader = provider.getObjectReader(
                        typeName, getObjectClass(), features | getFeatures()
                );

                if (reader == null) {
                    throw new JSONException("No suitable ObjectReader found for" + typeName);
                }
            }

            if (reader != this) {
                return reader.createInstance(map, features);
            }
        }

        return map;
    }

    @Override
    public Object readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isJSONB()) {
            return jsonReader.readAny();
        }

        JSONReader.Context context = jsonReader.getContext();

        String typeName = null;
        if (jsonReader.isObject()) {
            jsonReader.nextIfObjectStart();

            long hash = 0;
            if (jsonReader.isString()) {
                hash = jsonReader.readFieldNameHashCode();

                if (hash == HASH_TYPE) {
                    boolean supportAutoType = context.isEnabled(JSONReader.Feature.SupportAutoType);

                    ObjectReader autoTypeObjectReader;

                    if (supportAutoType) {
                        long typeHash = jsonReader.readTypeHashCode();
                        autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);

                        if (autoTypeObjectReader != null) {
                            Class objectClass = autoTypeObjectReader.getObjectClass();
                            if (objectClass != null) {
                                ClassLoader objectClassLoader = objectClass.getClassLoader();
                                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                                if (objectClassLoader != contextClassLoader) {
                                    Class contextClass = null;

                                    typeName = jsonReader.getString();
                                    try {
                                        contextClass = contextClassLoader.loadClass(typeName);
                                    } catch (ClassNotFoundException ignored) {
                                    }

                                    if (!objectClass.equals(contextClass)) {
                                        autoTypeObjectReader = context.getObjectReader(contextClass);
                                    }
                                }
                            }
                        }

                        if (autoTypeObjectReader == null) {
                            typeName = jsonReader.getString();
                            autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);
                        }
                    } else {
                        typeName = jsonReader.readString();
                        autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                        if (autoTypeObjectReader == null && jsonReader.getContext().isEnabled(JSONReader.Feature.ErrorOnNotSupportAutoType)) {
                            throw new JSONException(jsonReader.info("autoType not support : " + typeName));
                        }
                    }

                    if (autoTypeObjectReader != null) {
                        jsonReader.setTypeRedirect(true);

                        return autoTypeObjectReader.readObject(jsonReader, fieldType, fieldName, features);
                    }
                }
            }

            Map object;
            Supplier<Map> objectSupplier = jsonReader.getContext().getObjectSupplier();
            if (objectSupplier != null) {
                object = objectSupplier.get();
            } else {
                if (((features | context.getFeatures()) & JSONReader.Feature.UseNativeObject.mask) != 0) {
                    object = new HashMap();
                } else {
                    object = (Map) ObjectReaderImplMap.INSTANCE_OBJECT.createInstance(jsonReader.features(features));
                }
            }

            if (typeName != null) {
                switch (typeName) {
                    case "java.util.ImmutableCollections$Map1":
                    case "java.util.ImmutableCollections$MapN":
                        break;
                    default:
                        object.put("@type", typeName);
                        break;
                }
                hash = 0;
            }

            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfMatch('}')) {
                    break;
                }

                Object name;
                if (i == 0 && typeName == null && hash != 0) {
                    name = jsonReader.getFieldName();
                } else if (jsonReader.isNumber()) {
                    name = jsonReader.readNumber();
                    jsonReader.nextIfMatch(':');
                } else {
                    name = jsonReader.readFieldName();
                }
                if (name == null) {
                    name = jsonReader.readFieldNameUnquote();
                    if (jsonReader.current() == ':') {
                        jsonReader.next();
                    }
                }

                Object value;
                switch (jsonReader.current()) {
                    case '-':
                    case '+':
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '.':
                        value = jsonReader.readNumber();
                        break;
                    case '[':
                        value = jsonReader.readArray();
                        break;
                    case '{':
                        value = jsonReader.readObject();
                        break;
                    case '"':
                    case '\'':
                        value = jsonReader.readString();
                        break;
                    case 't':
                    case 'f':
                        value = jsonReader.readBoolValue();
                        break;
                    case 'n':
                        jsonReader.readNull();
                        value = null;
                        break;
                    case 'S':
                        if (jsonReader.nextIfSet()) {
                            value = jsonReader.read(HashSet.class);
                        } else {
                            throw new JSONException(jsonReader.info());
                        }
                        break;
                    default:
                        throw new JSONException(jsonReader.info());
                }

                Object origin = object.put(name, value);
                if (origin != null) {
                    long contextFeatures = features | context.getFeatures();
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

            return object;
        }

        Object value;
        switch (jsonReader.current()) {
            case '-':
            case '+':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '.':
                value = jsonReader.readNumber();
                break;
            case '[':
                value = jsonReader.readArray();
                break;
            case '"':
            case '\'':
                value = jsonReader.readString();
                break;
            case 't':
            case 'f':
                value = jsonReader.readBoolValue();
                break;
            case 'n':
                value = jsonReader.readNullOrNewDate();
                break;
            case 'S':
                if (jsonReader.nextIfSet()) {
                    HashSet<Object> set = new HashSet<>();
                    jsonReader.read(set);
                    value = set;
                } else {
                    throw new JSONException(jsonReader.info());
                }
                break;
            default:
                throw new JSONException(jsonReader.info());
        }

        return value;
    }

    @Override
    public Object readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        byte type = jsonReader.getType();
        if (type >= BC_STR_ASCII_FIX_MIN && type <= BC_STR_UTF16BE) {
            return jsonReader.readString();
        }

        if (type == BC_TYPED_ANY) {
            ObjectReader autoTypeObjectReader = jsonReader.checkAutoType(Object.class, 0, features);
            return autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (type == BC_NULL) {
            jsonReader.next();
            return null;
        }

        return jsonReader.readAny();
    }
}
