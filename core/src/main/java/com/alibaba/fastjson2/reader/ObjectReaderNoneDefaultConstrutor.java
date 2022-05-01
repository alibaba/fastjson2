package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONB.Constants.*;

class ObjectReaderNoneDefaultConstrutor<T>
        extends ObjectReaderAdapter<T> {
    final String[] paramNames;
    final FieldReader[] setterFieldReaders;
    private Function<Map<Long, Object>, T> creator;
    private List<Constructor> alternateConstructors;

    public ObjectReaderNoneDefaultConstrutor(
            Class objectClass
            , String typeName
            , long features
            , Function<Map<Long, Object>, T> creator
            , List<Constructor> alternateConstructors
            , String[] paramNames
            , FieldReader[] paramFieldReaders
            , FieldReader[] setterFieldReaders
    ) {
        super(objectClass, null, typeName, features, null, null, concat(paramFieldReaders, setterFieldReaders));
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
    public T readJSONBObject(JSONReader jsonReader, long features) {
        byte type = jsonReader.getType();
        if (type == BC_NULL) {
            jsonReader.next();
            return null;
        }

        if (type == BC_TYPED_ANY) {
            ObjectReader objectReader = jsonReader.checkAutoType(this.objectClass, typeNameHash, this.features | features);
            if (objectReader != this) {
                return (T) objectReader.readJSONBObject(jsonReader, features);
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
                    valueMap.put(fieldReader.getFieldNameHash(), fieldValue);
                }
            } else {
                throw new JSONException("expect object, but " + JSONB.typeName(jsonReader.getType()));
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
                            throw new JSONException("auotype not support : " + typeName);
                        }
                    }

                    Object object = (T) autoTypeObjectReader.readJSONBObject(jsonReader, features);
                    jsonReader.nextIfMatch(',');
                    return (T) object;
                }

                FieldReader fieldReader = getFieldReader(hashCode);
                if (fieldReader == null) {
                    jsonReader.skipValue();
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
                valueMap.put(fieldReader.getFieldNameHash(), fieldValue);
            }
        }

        Map<Long, Object> args
                = valueMap == null
                ? Collections.emptyMap()
                : valueMap;
        T object = createInstanceNoneDefaultConstructor(args);
        if (setterFieldReaders != null) {
            for (FieldReader fieldReader : setterFieldReaders) {
                Object fieldValue = valueMap.get(fieldReader.getFieldNameHash());
                fieldReader.accept(object, fieldValue);
            }
        }

        if (references != null) {
            for (Map.Entry<Long, String> entry : references.entrySet()) {
                Long hashCode = entry.getKey();
                String reference = entry.getValue();
                FieldReader fieldReader = getFieldReader(hashCode);
                if (reference.equals("..")) {
                    fieldReader.accept(object, object);
                    continue;
                }
                fieldReader.addResolveTask(jsonReader, object, reference);
            }
        }

        return object;
    }

    @Override
    public T readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, 0);
        }

        if (jsonReader.isArray() && jsonReader.isSupportBeanArray()) {
            jsonReader.next();
            LinkedHashMap<Long, Object> valueMap = null;
            for (int i = 0; i < fieldReaders.length; i++) {
                Object fieldValue = fieldReaders[i].readFieldValue(jsonReader);
                if (valueMap == null) {
                    valueMap = new LinkedHashMap<>();
                }
                long hash = fieldReaders[i].getFieldNameHash();
                valueMap.put(hash, fieldValue);
            }
            return createInstanceNoneDefaultConstructor(
                    valueMap == null
                            ? Collections.emptyMap()
                            : valueMap);
        }

        jsonReader.nextIfMatch('{');

        LinkedHashMap<Long, Object> valueMap = null;
        for (int i = 0; ; i++) {
            if (jsonReader.nextIfMatch('}')) {
                break;
            }

            long hashCode = jsonReader.readFieldNameHashCode();
            if (hashCode == 0) {
                continue;
            }

            if (hashCode == HASH_TYPE && i == 0) {
                long typeHash = jsonReader.readTypeHashCode();
                if (typeHash == typeNameHash) {
                    continue;
                }

                boolean supportAutoType = jsonReader.getContext().isEnable(JSONReader.Feature.SupportAutoType);

                JSONReader.Context context = jsonReader.getContext();
                ObjectReader autoTypeObjectReader = null;

                if (supportAutoType) {
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);

                    if (autoTypeObjectReader == null) {
                        String typeName = jsonReader.getString();
                        autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass);
                    }
                } else {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass);
                }

                if (autoTypeObjectReader == null) {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass, this.features | features | context.getFeatures());
                }

                if (autoTypeObjectReader != null) {
                    Object object = (T) autoTypeObjectReader.readObject(jsonReader, 0);
                    jsonReader.nextIfMatch(',');
                    return (T) object;
                }
                continue;
            }

            FieldReader fieldReader = getFieldReader(hashCode);
            if (fieldReader == null) {
                jsonReader.skipValue();
                continue;
            }

            Object fieldValue = fieldReader.readFieldValue(jsonReader);
            if (valueMap == null) {
                valueMap = new LinkedHashMap<>();
            }
            valueMap.put(fieldReader.getFieldNameHash(), fieldValue);
        }

        T object = createInstanceNoneDefaultConstructor(
                valueMap == null
                        ? Collections.emptyMap()
                        : valueMap);

        if (setterFieldReaders != null) {
            for (int i = 0; i < setterFieldReaders.length; i++) {
                FieldReader fieldReader = setterFieldReaders[i];
                Object fieldValue = valueMap.get(fieldReader.getFieldNameHash());
                if (fieldValue != null) {
                    fieldReader.accept(object, fieldValue);
                }
            }
        }

        jsonReader.nextIfMatch(',');

        return object;
    }

    @Override
    public T createInstance(Map map) {
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
                    Class fieldClass = fieldReader.getFieldClass();
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
                valueMap.put(fieldReader.getFieldNameHash(), fieldValue);
            }
        }

        return createInstanceNoneDefaultConstructor(
                valueMap == null
                        ? Collections.emptyMap()
                        : valueMap);
    }
}
