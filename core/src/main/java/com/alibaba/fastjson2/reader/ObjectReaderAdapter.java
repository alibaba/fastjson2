package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectReaderAdapter<T>
        extends ObjectReaderBean<T> {
    protected final String typeKey;
    protected final long typeKeyHashCode;

    final FieldReader[] fieldReaders;
    final long[] hashCodes;
    final short[] mapping;

    final long[] hashCodesLCase;
    final short[] mappingLCase;

    final Constructor constructor;
    volatile boolean instantiationError;

    // seeAlso
    final Class[] seeAlso;
    final String[] seeAlsoNames;
    final Map<Long, Class> seeAlsoMapping;

    public ObjectReaderAdapter(Class objectClass, Supplier<T> creator, FieldReader... fieldReaders) {
        this(objectClass, null, null, 0, null, creator, null, fieldReaders);
    }

    public ObjectReaderAdapter(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            JSONSchema schema,
            Supplier<T> creator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        this(
                objectClass,
                typeKey,
                typeName,
                features,
                schema,
                creator,
                buildFunction,
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
            JSONSchema schema,
            Supplier<T> creator,
            Function buildFunction,
            Class[] seeAlso,
            String[] seeAlsoNames,
            FieldReader... fieldReaders
    ) {
        super(objectClass, creator, typeName, features, schema, buildFunction);

        this.constructor = objectClass == null
                ? null
                : BeanUtils.getDefaultConstructor(objectClass, true);

        if (constructor != null) {
            constructor.setAccessible(true);
        }

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
                this.extraFieldReader = fieldReader;
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

    public Object auoType(JSONReader jsonReader, Class expectClass, long features) {
        long typeHash = jsonReader.readTypeHashCode();
        JSONReader.Context context = jsonReader.getContext();
        ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
        if (autoTypeObjectReader == null) {
            String typeName = jsonReader.getString();
            autoTypeObjectReader = context.getObjectReaderAutoType(typeName, expectClass, this.features | features | context.getFeatures());

            if (autoTypeObjectReader == null) {
                throw new JSONException(jsonReader.info("auotype not support : " + typeName));
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
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        jsonReader.nextIfMatch('[');
        Object object = creator.get();

        for (FieldReader fieldReader : fieldReaders) {
            fieldReader.readFieldValue(jsonReader, object);
        }

        if (!jsonReader.nextIfMatch(']')) {
            throw new JSONException(jsonReader.info("array to bean end error"));
        }

        jsonReader.nextIfMatch(',');

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

    public T createInstance(Collection collection) {
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
        if (instantiationError && constructor != null) {
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

        InstantiationException error;
        try {
            T object = (T) createInstance0(features);
            if (hasDefaultValue) {
                initDefaultValue(object);
            }
            return object;
        } catch (InstantiationException ex) {
            error = ex;
        }
        instantiationError = true;

        if (constructor != null) {
            try {
                T object = (T) constructor.newInstance();
                if (hasDefaultValue) {
                    initDefaultValue(object);
                }
                return object;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new JSONException("create instance error, " + objectClass, ex);
            }
        }

        throw new JSONException("create instance error, " + objectClass, error);
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
        JSONReader.Context context = jsonReader.getContext();
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

        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
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
                JSONReader.Context context = jsonReader.getContext();
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
                if (nameHashCodeLCase != hash) {
                    fieldReader = getFieldReaderLCase(nameHashCodeLCase);
                }
            }
            if (fieldReader == null) {
                processExtra(jsonReader, object);
                continue;
            }

            if (object == null) {
                object = createInstance(jsonReader.getContext().getFeatures() | features);
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        if (object == null) {
            object = createInstance(jsonReader.getContext().getFeatures() | features);
        }

        if (schema != null) {
            schema.assertValidate(object);
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
}
