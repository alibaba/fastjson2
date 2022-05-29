package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectReaderAdapter<T>
        extends ObjectReaderBean<T> {
    protected final String typeKey;
    protected final long typeKeyHashCode;
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
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            JSONSchema schema,
            Supplier<T> creator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        super(objectClass, typeName, schema);

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
            FieldReader fieldReader = fieldReaders[i];
            String fieldName = fieldReader.getFieldName();
            hashCodes[i] = Fnv.hashCode64(fieldName);
            hashCodesLCase[i] = Fnv.hashCode64LCase(fieldName);

            if (fieldReader.isUnwrapped()) {
                this.extraFieldReader = fieldReader;
            }

            if (fieldReader.getDefaultValue() != null) {
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

        for (FieldReader fieldReader : fieldReaders) {
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
        jsonReader.startArray();
        Object object = creator.get();

        for (FieldReader fieldReader : fieldReaders) {
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
    protected void initDefaultValue(T object) {
        for (FieldReader fieldReader : fieldReaders) {
            Object defaultValue = fieldReader.getDefaultValue();
            if (defaultValue != null) {
                fieldReader.accept(object, defaultValue);
            }
        }
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

        if (schema != null) {
            schema.assertValidate(object);
        }

        return (T) object;
    }
}
