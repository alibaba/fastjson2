package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONB.Constants.BC_NULL;
import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;

public class ObjectReaderNoneDefaultConstructor<T>
        extends ObjectReaderAdapter<T> {
    final String[] paramNames;
    final FieldReader[] setterFieldReaders;
    private Function<Map<Long, Object>, T> creator;
    private List<Constructor> alternateConstructors;

    public ObjectReaderNoneDefaultConstructor(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            Function<Map<Long, Object>, T> creator,
            List<Constructor> alternateConstructors,
            String[] paramNames,
            FieldReader[] paramFieldReaders,
            FieldReader[] setterFieldReaders,
            Class[] seeAlso,
            String[] seeAlsoNames
    ) {
        super(
                objectClass,
                typeKey,
                typeName,
                features,
                null,
                null,
                null,
                seeAlso,
                seeAlsoNames,
                concat(paramFieldReaders, setterFieldReaders)
        );

        this.paramNames = paramNames;
        this.creator = creator;
        this.setterFieldReaders = setterFieldReaders;
        this.alternateConstructors = alternateConstructors;
    }

    static FieldReader[] concat(FieldReader[] a, FieldReader[] b) {
        if (b == null) {
            return a;
        }
        int alen = a.length;
        a = Arrays.copyOf(a, alen + b.length);
        System.arraycopy(b, 0, a, alen, b.length);
        return a;
    }

    @Override
    public T createInstanceNoneDefaultConstructor(Map<Long, Object> values) {
        return creator.apply(values);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        byte type = jsonReader.getType();
        if (type == BC_NULL) {
            jsonReader.next();
            return null;
        }

        if (type == BC_TYPED_ANY) {
            ObjectReader objectReader = jsonReader.checkAutoType(this.objectClass, typeNameHash, this.features | features);
            if (objectReader != this) {
                return (T) objectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
            }
        }

        LinkedHashMap<Long, Object> valueMap = null;
        Map<Long, String> references = null;

        if (jsonReader.isArray()) {
            if (jsonReader.isSupportBeanArray()) {
                int entryCnt = jsonReader.startArray();
                for (int i = 0; i < entryCnt; ++i) {
                    FieldReader fieldReader = fieldReaders[i];
                    Object fieldValue = fieldReader.readFieldValue(jsonReader);
                    if (valueMap == null) {
                        valueMap = new LinkedHashMap<>();
                    }
                    valueMap.put(fieldReader.fieldNameHash, fieldValue);
                }
            } else {
                throw new JSONException(jsonReader.info("expect object, but " + JSONB.typeName(jsonReader.getType())));
            }
        } else {
            jsonReader.nextIfObjectStart();
            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfObjectEnd()) {
                    break;
                }

                long hashCode = jsonReader.readFieldNameHashCode();
                if (hashCode == 0) {
                    continue;
                }

                if (hashCode == HASH_TYPE && i == 0) {
                    long typeHash = jsonReader.readTypeHashCode();
                    JSONReader.Context context = jsonReader.getContext();
                    ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                    if (autoTypeObjectReader == null) {
                        String typeName = jsonReader.getString();
                        autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass);

                        if (autoTypeObjectReader == null) {
                            throw new JSONException(jsonReader.info("auotype not support : " + typeName));
                        }
                    }

                    Object object = (T) autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
                    jsonReader.nextIfMatch(',');
                    return (T) object;
                }

                FieldReader fieldReader = getFieldReader(hashCode);
                if (fieldReader == null) {
                    processExtra(jsonReader, null);
                    continue;
                }

                Object fieldValue;
                if (jsonReader.isReference()) {
                    jsonReader.next();
                    String reference = jsonReader.readString();
                    if (references == null) {
                        references = new HashMap<>();
                    }
                    references.put(hashCode, reference);
                    continue;
                }

                fieldValue = fieldReader.readFieldValue(jsonReader);
                if (valueMap == null) {
                    valueMap = new LinkedHashMap<>();
                }
                valueMap.put(fieldReader.fieldNameHash, fieldValue);
            }
        }

        Map<Long, Object> args
                = valueMap == null
                ? Collections.emptyMap()
                : valueMap;
        T object = createInstanceNoneDefaultConstructor(args);
        if (setterFieldReaders != null) {
            for (FieldReader fieldReader : setterFieldReaders) {
                Object fieldValue = args.get(fieldReader.fieldNameHash);
                fieldReader.accept(object, fieldValue);
            }
        }

        if (references != null) {
            for (Map.Entry<Long, String> entry : references.entrySet()) {
                Long hashCode = entry.getKey();
                String reference = entry.getValue();
                FieldReader fieldReader = getFieldReader(hashCode);
                if ("..".equals(reference)) {
                    fieldReader.accept(object, object);
                    continue;
                }
                fieldReader.addResolveTask(jsonReader, object, reference);
            }
        }

        return object;
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        if (jsonReader.isSupportBeanArray(features | this.features)
                && jsonReader.nextIfMatch('[')) {
            LinkedHashMap<Long, Object> valueMap = null;
            for (int i = 0; i < fieldReaders.length; i++) {
                Object fieldValue = fieldReaders[i].readFieldValue(jsonReader);
                if (valueMap == null) {
                    valueMap = new LinkedHashMap<>();
                }
                long hash = fieldReaders[i].fieldNameHash;
                valueMap.put(hash, fieldValue);
            }

            if (!jsonReader.nextIfMatch(']')) {
                throw new JSONException(jsonReader.info("array not end, " + jsonReader.current()));
            }

            jsonReader.nextIfMatch(',');
            return createInstanceNoneDefaultConstructor(
                    valueMap == null
                            ? Collections.emptyMap()
                            : valueMap);
        }

        boolean objectStart = jsonReader.nextIfObjectStart();
        if (!objectStart && !jsonReader.isTypeRedirect()) {
            if (jsonReader.nextIfNullOrEmptyString()) {
                return null;
            }
        }
        JSONReader.Context context = jsonReader.getContext();
        long featuresAll = this.features | features | context.getFeatures();

        LinkedHashMap<Long, Object> valueMap = null;
        for (int i = 0; ; i++) {
            if (jsonReader.nextIfMatch('}')) {
                break;
            }

            long hashCode = jsonReader.readFieldNameHashCode();
            if (hashCode == 0) {
                continue;
            }

            if (hashCode == typeKeyHashCode && i == 0) {
                long typeHash = jsonReader.readTypeHashCode();
                if (typeHash == typeNameHash) {
                    continue;
                }

                boolean supportAutoType = (featuresAll & JSONReader.Feature.SupportAutoType.mask) != 0;

                ObjectReader autoTypeObjectReader;

                if (supportAutoType) {
                    autoTypeObjectReader = autoType(context, typeHash);

                    if (autoTypeObjectReader == null) {
                        String typeName = jsonReader.getString();
                        autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass, this.features);
                    }
                } else {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass);
                }

                if (autoTypeObjectReader == null) {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass, this.features);
                }

                if (autoTypeObjectReader != null) {
                    Object object = (T) autoTypeObjectReader.readObject(jsonReader, fieldType, fieldName, 0);
                    jsonReader.nextIfMatch(',');
                    return (T) object;
                }
                continue;
            }

            FieldReader fieldReader = getFieldReader(hashCode);

            if (fieldReader == null && (featuresAll & JSONReader.Feature.SupportSmartMatch.mask) != 0) {
                long hashCodeLCase = jsonReader.getNameHashCodeLCase();
                if (hashCodeLCase != hashCode) {
                    fieldReader = getFieldReaderLCase(hashCodeLCase);
                }
            }

            if (fieldReader == null) {
                processExtra(jsonReader, null);
                continue;
            }

            Object fieldValue = fieldReader.readFieldValue(jsonReader);
            if (valueMap == null) {
                valueMap = new LinkedHashMap<>();
            }

            long hash;
            if (fieldReader instanceof FieldReaderObjectParam) {
                hash = ((FieldReaderObjectParam<?>) fieldReader).paramNameHash;
            } else {
                hash = fieldReader.fieldNameHash;
            }
            valueMap.put(hash, fieldValue);
        }

        Map<Long, Object> argsMap = valueMap == null ? Collections.emptyMap() : valueMap;
        T object = creator.apply(argsMap);

        if (setterFieldReaders != null && valueMap != null) {
            for (int i = 0; i < setterFieldReaders.length; i++) {
                FieldReader fieldReader = setterFieldReaders[i];
                Object fieldValue = valueMap.get(fieldReader.fieldNameHash);
                if (fieldValue != null) {
                    fieldReader.accept(object, fieldValue);
                }
            }
        }

        jsonReader.nextIfMatch(',');

        return object;
    }

    public T readFromCSV(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        LinkedHashMap<Long, Object> valueMap = new LinkedHashMap<>();
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fieldReader = fieldReaders[i];
            Object fieldValue = fieldReader.readFieldValue(jsonReader);
            valueMap.put(fieldReader.fieldNameHash, fieldValue);
        }

        jsonReader.nextIfMatch('\n');

        return createInstanceNoneDefaultConstructor(valueMap);
    }

    public T createInstance(Collection collection) {
        int index = 0;

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        LinkedHashMap<Long, Object> valueMap = new LinkedHashMap<>();
        for (Iterator it = collection.iterator(); it.hasNext();) {
            Object fieldValue = it.next();
            if (index >= fieldReaders.length) {
                break;
            }
            FieldReader fieldReader = fieldReaders[index];

            if (fieldValue != null) {
                Class<?> valueClass = fieldValue.getClass();
                Class fieldClass = fieldReader.fieldClass;
                if (valueClass != fieldClass) {
                    Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);
                    if (typeConvert != null) {
                        fieldValue = typeConvert.apply(fieldValue);
                    }
                }
            }

            if (valueMap == null) {
                valueMap = new LinkedHashMap<>();
            }

            long hash;
            if (fieldReader instanceof FieldReaderObjectParam) {
                hash = ((FieldReaderObjectParam<?>) fieldReader).paramNameHash;
            } else {
                hash = fieldReader.fieldNameHash;
            }
            valueMap.put(hash, fieldValue);

            index++;
        }

        T object = createInstanceNoneDefaultConstructor(
                valueMap == null
                        ? Collections.emptyMap()
                        : valueMap
        );

        return object;
    }

    @Override
    public T createInstance(Map map, long features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        LinkedHashMap<Long, Object> valueMap = null;

        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = it.next();
            String fieldName = entry.getKey().toString();
            Object fieldValue = entry.getValue();

            FieldReader fieldReader = getFieldReader(fieldName);
            if (fieldReader != null) {
                if (fieldValue != null) {
                    Class<?> valueClass = fieldValue.getClass();
                    Class fieldClass = fieldReader.fieldClass;
                    if (valueClass != fieldClass) {
                        Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);
                        if (typeConvert != null) {
                            fieldValue = typeConvert.apply(fieldValue);
                        }
                    }
                }

                if (valueMap == null) {
                    valueMap = new LinkedHashMap<>();
                }

                long hash;
                if (fieldReader instanceof FieldReaderObjectParam) {
                    hash = ((FieldReaderObjectParam<?>) fieldReader).paramNameHash;
                } else {
                    hash = fieldReader.fieldNameHash;
                }
                valueMap.put(hash, fieldValue);
            }
        }

        T object = createInstanceNoneDefaultConstructor(
                valueMap == null
                        ? Collections.emptyMap()
                        : valueMap
        );

        for (int i = 0; i < setterFieldReaders.length; i++) {
            FieldReader fieldReader = setterFieldReaders[i];
            Object fieldValue = map.get(fieldReader.fieldName);
            if (fieldValue == null) {
                continue;
            }

            Class<?> valueClass = fieldValue.getClass();
            Class fieldClass = fieldReader.fieldClass;
            if (valueClass != fieldClass) {
                Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);
                if (typeConvert != null) {
                    fieldValue = typeConvert.apply(fieldValue);
                } else if (fieldValue instanceof Map) {
                    ObjectReader objectReader = fieldReader.getObjectReader(JSONFactory.createReadContext(provider));
                    fieldValue = objectReader.createInstance((Map) fieldValue, features | fieldReader.features);
                }
            }
            fieldReader.accept(object, fieldValue);
        }

        return object;
    }
}
