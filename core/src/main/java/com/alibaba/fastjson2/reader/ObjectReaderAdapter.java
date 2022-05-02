package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectReaderAdapter<T> extends ObjectReaderBean<T> {
    final protected String typeKey;
    final protected long typeKeyHashCode;
    final long features;
    final Supplier<T> creator;
    final Function buildFunction;
    final FieldReader[] fieldReaders;
    final long[] hashCodes;
    final short[] mapping;

    final long[] hashCodesLCase;
    final short[] mappingLCase;

    final Constructor constructor;
    volatile boolean instantiationError;

    public ObjectReaderAdapter(
            Class objectClass
            , String typeKey
            , String typeName
            , long features
            , Supplier<T> creator
            , Function buildFunction
            , FieldReader... fieldReaders
    ) {
        super(objectClass, typeName);

        this.constructor = objectClass == null
                ? null
                : BeanUtils.getDefaultConstructor(objectClass);

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

        this.features = features;
        this.creator = creator;
        this.buildFunction = buildFunction;
        this.fieldReaders = fieldReaders;

        long[] hashCodes = new long[fieldReaders.length];
        long[] hashCodesLCase = new long[fieldReaders.length];
        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader item = fieldReaders[i];
            String fieldName = item.getFieldName();
            hashCodes[i] = Fnv.hashCode64(fieldName);
            hashCodesLCase[i] = Fnv.hashCode64LCase(fieldName);
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
    }

    @Override
    public String getTypeKey() {
        return typeKey;
    }

    @Override
    public long getTypeKeyHash() {
        return typeKeyHashCode;
    }

    @Override
    public long getFeatures() {
        return features;
    }

    @Override
    public Class getObjectClass() {
        return objectClass;
    }

    public Object auoType(JSONReader jsonReader, Class expectClass, long features) {
        long typeHash = jsonReader.readTypeHashCode();
        JSONReader.Context context = jsonReader.getContext();
        ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
        if (autoTypeObjectReader == null) {
            String typeName = jsonReader.getString();
            autoTypeObjectReader = context.getObjectReaderAutoType(typeName, expectClass, this.features | features | context.getFeatures());

            if (autoTypeObjectReader == null) {
                throw new JSONException("auotype not support : " + typeName);
            }
        }

        return autoTypeObjectReader.readObject(jsonReader, features);
    }

    @Override
    public Function getBuildFunction() {
        return buildFunction;
    }

    @Override
    public T readArrayMappingObject(JSONReader jsonReader) {
        jsonReader.nextIfMatch('[');
        Object object = creator.get();

        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fieldReader = fieldReaders[i];
            fieldReader.readFieldValue(jsonReader, object);
        }

        if (!jsonReader.nextIfMatch(']')) {
            throw new JSONException("array to bean end error, " + jsonReader.current());
        }

        jsonReader.nextIfMatch(',');

        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }
        return (T) object;
    }

    @Override
    public T readArrayMappingJSONBObject(JSONReader jsonReader) {
        int entryCnt = jsonReader.startArray();
        Object object = creator.get();

        for (int i = 0; i < fieldReaders.length; i++) {
            FieldReader fieldReader = fieldReaders[i];
            fieldReader.readFieldValue(jsonReader, object);
        }

        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return (T) object;
    }

    protected Object createInstance0(long features) throws InstantiationException {
        if (creator == null) {
            throw new JSONException("create instance error, " + objectClass);
        }
        return creator.get();
    }

    @Override
    public T createInstance(long features) {
        if (instantiationError) {
            if (constructor != null) {
                try {
                    return (T) constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    throw new JSONException("create instance error, " + objectClass, ex);
                }
            }
        }

        if ((features & JSONReader.Feature.UseDefaultConstructorAsPossible.mask) != 0 && constructor.getParameterCount() == 0) {
            if (constructor != null) {
                try {
                    return (T) constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    throw new JSONException("create instance error, " + objectClass, ex);
                }
            }
        }

        InstantiationException error;
        try {
            return (T) createInstance0(features);
        } catch (InstantiationException ex) {
            error = ex;
        }
        instantiationError = true;

        if (constructor != null) {
            try {
                return (T) constructor.newInstance();
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
                throw new JSONException("auotype not support : " + typeName);
            }
        }

        return (T) autoTypeObjectReader.readJSONBObject(jsonReader, features);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, features);
        }

        if (jsonReader.isArray()) {
            if (jsonReader.isSupportBeanArray()) {
                return readArrayMappingJSONBObject(jsonReader);
            } else {
                throw new JSONException("expect object, but " + JSONB.typeName(jsonReader.getType()));
            }
        }

        jsonReader.nextIfObjectStart();

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
                        throw new JSONException("auotype not support : " + typeName);
                    }
                }

                if (autoTypeObjectReader == this) {
                    continue;
                }

                jsonReader.setTypeRedirect(true);
                return (T) autoTypeObjectReader.readJSONBObject(jsonReader, features);
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
                jsonReader.skipValue();
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
        return (T) object;
    }
}
