package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectReaderAdapter<T>
        extends ObjectReaderBean<T> {
    protected final String typeKey;
    protected final long typeKeyHashCode;

    protected final FieldReader[] fieldReaders;
    final long[] hashCodes;
    final short[] mapping;

    final long[] hashCodesLCase;
    final short[] mappingLCase;

    final Constructor constructor;
    final int parameterCount;

    // seeAlso
    final Class[] seeAlso;
    final String[] seeAlsoNames;
    final Class seeAlsoDefault;
    final Map<Long, Class> seeAlsoMapping;

    public ObjectReaderAdapter(Class objectClass, Supplier<T> creator, FieldReader... fieldReaders) {
        this(objectClass, null, null, 0, creator, null, fieldReaders);
    }

    public ObjectReaderAdapter(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            Supplier<T> creator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        this(
                objectClass,
                typeKey,
                typeName,
                features,
                creator,
                buildFunction,
                null,
                null,
                null,
                fieldReaders
        );
    }

    public ObjectReaderAdapter(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            Supplier<T> creator,
            Function buildFunction,
            Class[] seeAlso,
            String[] seeAlsoNames,
            FieldReader... fieldReaders
    ) {
        this(
                objectClass,
                typeKey,
                typeName,
                features,
                creator,
                buildFunction,
                seeAlso,
                seeAlsoNames,
                null,
                fieldReaders
        );
    }

    public ObjectReaderAdapter(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            Supplier<T> creator,
            Function buildFunction,
            Class[] seeAlso,
            String[] seeAlsoNames,
            Class seeAlsoDefault,
            FieldReader... fieldReaders
    ) {
        super(objectClass, creator, typeName, features, buildFunction);

        Constructor constructor;
        if (creator instanceof ConstructorSupplier) {
            constructor = ((ConstructorSupplier) creator).constructor;
        } else {
            constructor = objectClass == null
                    ? null
                    : BeanUtils.getDefaultConstructor(objectClass, true);
            if (constructor != null) {
                constructor.setAccessible(true);
            }
        }

        if (constructor != null) {
            parameterCount = constructor.getParameterCount();
        } else {
            parameterCount = -1;
        }
        this.constructor = constructor;

        if (typeKey == null || typeKey.isEmpty()) {
            this.typeKey = "@type";
            typeKeyHashCode = HASH_TYPE;
        } else {
            this.typeKey = typeKey;
            typeKeyHashCode = Fnv.hashCode64(typeKey);
        }

        this.fieldReaders = fieldReaders;

        long[] hashCodes = new long[fieldReaders.length];
        long[] hashCodesLCase = new long[fieldReaders.length];
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fieldReader = fieldReaders[i];
            hashCodes[i] = fieldReader.fieldNameHash;
            hashCodesLCase[i] = fieldReader.fieldNameHashLCase;

            if (fieldReader.isUnwrapped()) {
                if (this.extraFieldReader == null || !(this.extraFieldReader instanceof FieldReaderAnySetter)) {
                    this.extraFieldReader = fieldReader;
                }
            }

            if (fieldReader.defaultValue != null) {
                this.hasDefaultValue = true;
            }
        }

        this.hashCodes = Arrays.copyOf(hashCodes, hashCodes.length);
        Arrays.sort(this.hashCodes);

        mapping = new short[this.hashCodes.length];
        for (int i = 0; i < hashCodes.length; i++) {
            long hashCode = hashCodes[i];
            int index = Arrays.binarySearch(this.hashCodes, hashCode);
            mapping[index] = (short) i;
        }

        this.hashCodesLCase = Arrays.copyOf(hashCodesLCase, hashCodesLCase.length);
        Arrays.sort(this.hashCodesLCase);

        mappingLCase = new short[this.hashCodesLCase.length];
        for (int i = 0; i < hashCodesLCase.length; i++) {
            long hashCode = hashCodesLCase[i];
            int index = Arrays.binarySearch(this.hashCodesLCase, hashCode);
            mappingLCase[index] = (short) i;
        }

        this.seeAlso = seeAlso;
        if (seeAlso != null) {
            this.seeAlsoMapping = new HashMap<>(seeAlso.length);
            this.seeAlsoNames = new String[seeAlso.length];
            for (int i = 0; i < seeAlso.length; i++) {
                Class seeAlsoClass = seeAlso[i];

                String seeAlsoTypeName = null;
                if (seeAlsoNames != null && seeAlsoNames.length >= i + 1) {
                    seeAlsoTypeName = seeAlsoNames[i];
                }
                if (seeAlsoTypeName == null || seeAlsoTypeName.isEmpty()) {
                    seeAlsoTypeName = seeAlsoClass.getSimpleName();
                }
                long hashCode = Fnv.hashCode64(seeAlsoTypeName);
                seeAlsoMapping.put(hashCode, seeAlsoClass);
                this.seeAlsoNames[i] = seeAlsoTypeName;
            }
        } else {
            this.seeAlsoMapping = null;
            this.seeAlsoNames = null;
        }
        this.seeAlsoDefault = seeAlsoDefault;
    }

    @Override
    public final String getTypeKey() {
        return typeKey;
    }

    @Override
    public final long getTypeKeyHash() {
        return typeKeyHashCode;
    }

    @Override
    public final long getFeatures() {
        return features;
    }

    public FieldReader[] getFieldReaders() {
        return Arrays.copyOf(this.fieldReaders, this.fieldReaders.length);
    }

    public Object autoType(JSONReader jsonReader, Class expectClass, long features) {
        long typeHash = jsonReader.readTypeHashCode();
        JSONReader.Context context = jsonReader.context;

        ObjectReader autoTypeObjectReader = null;
        if (jsonReader.isSupportAutoTypeOrHandler(features)) {
            autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
        }
        if (autoTypeObjectReader == null) {
            String typeName = jsonReader.getString();
            autoTypeObjectReader = context.getObjectReaderAutoType(typeName, expectClass, this.features | features | context.features);

            if (autoTypeObjectReader == null) {
                if (expectClass == objectClass) {
                    autoTypeObjectReader = this;
                } else {
                    throw new JSONException(jsonReader.info("auotype not support : " + typeName));
                }
            }
        }

        return autoTypeObjectReader.readObject(jsonReader, null, null, features);
    }

    @Override
    public final Function getBuildFunction() {
        return buildFunction;
    }

    @Override
    public T readArrayMappingObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.jsonb) {
            return readArrayMappingJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        jsonReader.nextIfArrayStart();
        Object object = creator.get();

        for (int i = 0; i < fieldReaders.length; i++) {
            fieldReaders[i].readFieldValue(jsonReader, object);
        }

        if (!jsonReader.nextIfArrayEnd()) {
            throw new JSONException(jsonReader.info("array to bean end error"));
        }

        jsonReader.nextIfComma();

        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return (T) object;
    }

    @Override
    public T readArrayMappingJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        ObjectReader autoTypeReader = checkAutoType(jsonReader, this.objectClass, this.features | features);
        if (autoTypeReader != null && autoTypeReader != this && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readArrayMappingJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        int entryCnt = jsonReader.startArray();
        Object object = createInstance(0);

        for (int i = 0; i < fieldReaders.length; i++) {
            if (i >= entryCnt) {
                continue;
            }
            FieldReader fieldReader = fieldReaders[i];
            fieldReader.readFieldValue(jsonReader, object);
        }

        for (int i = fieldReaders.length; i < entryCnt; i++) {
            jsonReader.skipValue();
        }

        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return (T) object;
    }

    protected Object createInstance0(long features) throws InstantiationException {
        if ((features & JSONReader.Feature.UseDefaultConstructorAsPossible.mask) != 0
                && constructor != null
                && constructor.getParameterCount() == 0) {
            T object;
            try {
                object = (T) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new JSONException("create instance error, " + objectClass, ex);
            }

            if (hasDefaultValue) {
                initDefaultValue(object);
            }

            return object;
        }

        if (creator == null) {
            throw new JSONException("create instance error, " + objectClass);
        }
        return creator.get();
    }

    @Override
    protected void initDefaultValue(T object) {
        for (FieldReader fieldReader : fieldReaders) {
            Object defaultValue = fieldReader.defaultValue;
            if (defaultValue != null) {
                fieldReader.accept(object, defaultValue);
            }
        }
    }

    public T createInstance(Collection collection, long features) {
        T object = createInstance(0L);
        int index = 0;
        for (Iterator it = collection.iterator(); it.hasNext(); ) {
            Object fieldValue = it.next();
            if (index >= fieldReaders.length) {
                break;
            }
            FieldReader fieldReader = fieldReaders[index];
            fieldReader.accept(object, fieldValue);
            index++;
        }
        return object;
    }

    @Override
    public T createInstance(long features) {
        T object;
        if (constructor != null && parameterCount == 0) {
            try {
                object = (T) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new JSONException("create instance error, " + objectClass, ex);
            }
        } else {
            if (creator == null) {
                throw new JSONException("create instance error, " + objectClass);
            }
            object = creator.get();
        }

        if (hasDefaultValue) {
            initDefaultValue(object);
        }
        return object;
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        int m = Arrays.binarySearch(hashCodes, hashCode);
        if (m < 0) {
            return null;
        }

        int index = this.mapping[m];
        return fieldReaders[index];
    }

    protected final void readFieldValue(long hashCode, JSONReader jsonReader, long features, Object object) {
        FieldReader fieldReader = getFieldReader(hashCode);
        if (fieldReader == null
                && !disableSmartMatch
                && jsonReader.isSupportSmartMatch(this.features | features)) {
            long hashCodeL = jsonReader.getNameHashCodeLCase();
            fieldReader = getFieldReaderLCase(hashCodeL == hashCode ? hashCode : hashCodeL);
        }

        if (fieldReader != null) {
            fieldReader.readFieldValue(jsonReader, object);
        } else {
            processExtra(jsonReader, object, 0);
        }
    }

    @Override
    public FieldReader getFieldReaderLCase(long hashCode) {
        int m = Arrays.binarySearch(hashCodesLCase, hashCode);
        if (m < 0) {
            return null;
        }

        int index = this.mappingLCase[m];
        return fieldReaders[index];
    }

    protected T autoType(JSONReader jsonReader) {
        long typeHash = jsonReader.readTypeHashCode();
        JSONReader.Context context = jsonReader.context;
        ObjectReader autoTypeObjectReader = autoType(context, typeHash);
        if (autoTypeObjectReader == null) {
            String typeName = jsonReader.getString();
            autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

            if (autoTypeObjectReader == null) {
                throw new JSONException(jsonReader.info("auotype not support : " + typeName));
            }
        }

        return (T) autoTypeObjectReader.readJSONBObject(jsonReader, null, null, features);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.getTypeNameHash(), this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.isArray()) {
            if (jsonReader.isSupportBeanArray()) {
                return readArrayMappingJSONBObject(jsonReader, fieldType, fieldName, features);
            } else {
                throw new JSONException(jsonReader.info("expect object, but " + JSONB.typeName(jsonReader.getType())));
            }
        }

        boolean objectStart = jsonReader.nextIfObjectStart();

        Object object = null;
        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long hash = jsonReader.readFieldNameHashCode();
            if (hash == typeKeyHashCode && i == 0) {
                long typeHash = jsonReader.readValueHashCode();
                JSONReader.Context context = jsonReader.context;
                ObjectReader autoTypeObjectReader = autoType(context, typeHash);
                if (autoTypeObjectReader == null) {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, null);

                    if (autoTypeObjectReader == null) {
                        throw new JSONException(jsonReader.info("auotype not support : " + typeName));
                    }
                }

                if (autoTypeObjectReader == this) {
                    continue;
                }

                jsonReader.setTypeRedirect(true);
                return (T) autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
            }

            if (hash == 0) {
                continue;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | this.features)) {
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                fieldReader = getFieldReaderLCase(nameHashCodeLCase);
            }
            if (fieldReader == null) {
                processExtra(jsonReader, object, features);
                continue;
            }

            if (object == null) {
                object = createInstance(jsonReader.context.features | features);
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        if (object == null) {
            object = createInstance(jsonReader.context.features | features);
        }

        return (T) object;
    }

    @Override
    public ObjectReader autoType(ObjectReaderProvider provider, long typeHash) {
        if (seeAlsoMapping != null && seeAlsoMapping.size() > 0) {
            Class seeAlsoClass = seeAlsoMapping.get(typeHash);
            if (seeAlsoClass == null) {
                return null;
            }
            return provider.getObjectReader(seeAlsoClass);
        }

        return provider.getObjectReader(typeHash);
    }

    @Override
    public ObjectReader autoType(JSONReader.Context context, long typeHash) {
        if (seeAlsoMapping != null && seeAlsoMapping.size() > 0) {
            Class seeAlsoClass = seeAlsoMapping.get(typeHash);
            if (seeAlsoClass == null) {
                return null;
            }

            return context.getObjectReader(seeAlsoClass);
        }

        return context.getObjectReaderAutoType(typeHash);
    }

    protected void initStringFieldAsEmpty(Object object) {
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fieldReader = fieldReaders[i];
            if (fieldReader.fieldClass == String.class) {
                fieldReader.accept(object, "");
            }
        }
    }

    public T createInstance(Map map, long features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Object typeKey = map.get(this.typeKey);

        long features2 = features | this.features;
        if (typeKey instanceof String) {
            String typeName = (String) typeKey;
            long typeHash = Fnv.hashCode64(typeName);
            ObjectReader<T> reader = null;
            if ((features & JSONReader.Feature.SupportAutoType.mask) != 0 || this instanceof ObjectReaderSeeAlso) {
                reader = autoType(provider, typeHash);
            }

            if (reader == null) {
                reader = provider.getObjectReader(
                        typeName, getObjectClass(), features2
                );
            }

            if (reader != this && reader != null) {
                return reader.createInstance(map, features);
            }
        }

        T object = createInstance(0L);

        if (extraFieldReader == null
                && (features2 & (JSONReader.Feature.SupportSmartMatch.mask | JSONReader.Feature.ErrorOnUnknownProperties.mask)) == 0
        ) {
            boolean fieldBased = (features2 & JSONReader.Feature.FieldBased.mask) != 0;
            for (int i = 0; i < fieldReaders.length; i++) {
                FieldReader fieldReader = fieldReaders[i];
                Object fieldValue = map.get(fieldReader.fieldName);
                if (fieldValue == null) {
                    continue;
                }

                if (fieldReader.field != null && Modifier.isFinal(fieldReader.field.getModifiers())) {
                    try {
                        Object value = fieldReader.method.invoke(object);
                        if (value instanceof Collection && !((Collection) value).isEmpty()) {
                            continue;
                        }
                    } catch (Exception e) {
                        // just ignore
                    }
                }

                if (fieldValue.getClass() == fieldReader.fieldType) {
                    fieldReader.accept(object, fieldValue);
                } else {
                    if ((fieldReader instanceof FieldReaderList)
                            && fieldValue instanceof JSONArray
                    ) {
                        ObjectReader objectReader = fieldReader.getObjectReader(provider);
                        Object fieldValueList = objectReader.createInstance((JSONArray) fieldValue, features);
                        fieldReader.accept(object, fieldValueList);
                        continue;
                    } else if (fieldValue instanceof JSONObject
                            && fieldReader.fieldType != JSONObject.class
                    ) {
                        JSONObject jsonObject = (JSONObject) fieldValue;
                        Object fieldValueJavaBean = provider
                                .getObjectReader(fieldReader.fieldType, fieldBased)
                                .createInstance(jsonObject, features);
                        fieldReader.accept(object, fieldValueJavaBean);
                        continue;
                    }

                    fieldReader.acceptAny(object, fieldValue, features);
                }
            }
        } else {
            for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
                String entryKey = entry.getKey().toString();
                Object fieldValue = entry.getValue();

                FieldReader fieldReader = getFieldReader(entryKey);
                if (fieldReader == null) {
                    acceptExtra(object, entryKey, entry.getValue(), features);
                    continue;
                }

                if (fieldValue != null
                        && fieldValue.getClass() == fieldReader.fieldType
                ) {
                    fieldReader.accept(object, fieldValue);
                } else {
                    fieldReader.acceptAny(object, fieldValue, features);
                }
            }
        }

        return buildFunction != null
                ? (T) buildFunction.apply(object)
                : object;
    }
}
