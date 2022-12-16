package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT;
import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT_END;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE_SUPPORT;

public class ObjectReader1<T>
        extends ObjectReaderAdapter<T> {
    protected final FieldReader fieldReader0;
    final long hashCode0;
    final long hashCode0LCase;

    protected ObjectReader objectReader0;

    public ObjectReader1(
            Class objectClass,
            long features,
            JSONSchema schema,
            Supplier<T> creator,
            Function buildFunction,
            FieldReader fieldReader
    ) {
        this(objectClass, null, null, features, schema, creator, buildFunction, fieldReader);
    }

    public ObjectReader1(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            JSONSchema schema,
            Supplier<T> creator,
            Function buildFunction,
            FieldReader... fieldReaders
    ) {
        super(objectClass, typeKey, typeName, features, schema, creator, buildFunction, fieldReaders);

        this.fieldReader0 = fieldReaders[0];
        this.hashCode0 = fieldReader0.fieldNameHash;
        this.hashCode0LCase = fieldReader0.fieldNameHashLCase;

        if (fieldReader0.isUnwrapped()) {
            extraFieldReader = fieldReader0;
        }

        hasDefaultValue = fieldReader0.defaultValue != null;
    }

    @Override
    public T readObject(JSONReader jsonReader) {
        return readObject(jsonReader, null, null, features);
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

        Object object = creator.get();

        int entryCnt = jsonReader.startArray();
        if (entryCnt > 0) {
            fieldReader0.readFieldValue(jsonReader, object);
            for (int i = 1; i < entryCnt; ++i) {
                jsonReader.skipValue();
            }
        }

        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return (T) object;
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        ObjectReader autoTypeReader = checkAutoType(jsonReader, this.objectClass, this.features | features);
        if (autoTypeReader != null && autoTypeReader != this && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (jsonReader.isArray()) {
            Object object = creator.get();
            int entryCnt = jsonReader.startArray();
            if (entryCnt > 0) {
                fieldReader0.readFieldValue(jsonReader, object);
                for (int i = 1; i < entryCnt; ++i) {
                    jsonReader.skipValue();
                }
            }

            if (buildFunction != null) {
                return (T) buildFunction.apply(object);
            }
            return (T) object;
        }

        if (!jsonReader.nextIfMatch(BC_OBJECT)) {
            if (jsonReader.isTypeRedirect()) {
                jsonReader.setTypeRedirect(false);
            } else {
                throw new JSONException(jsonReader.info("expect object, but " + JSONB.typeName(jsonReader.getType())));
            }
        }

        T object;
        if (creator != null) {
            object = creator.get();
        } else if (UNSAFE_SUPPORT && ((features | jsonReader.getContext().getFeatures()) & JSONReader.Feature.FieldBased.mask) != 0) {
            try {
                object = (T) UnsafeUtils.UNSAFE.allocateInstance(objectClass);
            } catch (InstantiationException e) {
                throw new JSONException(jsonReader.info("create instance error"), e);
            }
        } else {
            object = null;
        }

        if (object != null && hasDefaultValue) {
            initDefaultValue(object);
        }

        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfMatch(BC_OBJECT_END)) {
                break;
            }

            long hashCode = jsonReader.readFieldNameHashCode();

            if (hashCode == getTypeKeyHash() && i == 0) {
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

                if (autoTypeObjectReader == this) {
                    continue;
                }

                return (T) autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
            }

            if (hashCode == 0) {
                continue;
            }

            if (hashCode == this.hashCode0) {
                fieldReader0.readFieldValueJSONB(jsonReader, object);
            } else {
                if (jsonReader.isSupportSmartMatch(features | this.features)
                        && jsonReader.getNameHashCodeLCase() == this.hashCode0LCase) {
                    fieldReader0.readFieldValue(jsonReader, object);
                } else {
                    processExtra(jsonReader, object);
                }
            }
        }

        if (buildFunction != null) {
            object = (T) buildFunction.apply(object);
        }

        if (schema != null) {
            schema.assertValidate(object);
        }

        return object;
    }

    @Override
    protected void initDefaultValue(T object) {
        fieldReader0.acceptDefaultValue(object);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        if (jsonReader.nextIfNullOrEmptyString()) {
            return null;
        }

        long featuresAll = jsonReader.features(this.features | features);
        if (jsonReader.isArray()) {
            if ((featuresAll & JSONReader.Feature.SupportArrayToBean.mask) != 0) {
                jsonReader.next();
                Object object = creator.get();

                fieldReader0.readFieldValue(jsonReader, object);
                if (!jsonReader.nextIfMatch(']')) {
                    throw new JSONException(jsonReader.info("array to bean end error, " + jsonReader.current()));
                }

                jsonReader.nextIfMatch(',');

                if (buildFunction != null) {
                    return (T) buildFunction.apply(object);
                }
                return (T) object;
            }

            return processObjectInputSingleItemArray(jsonReader, fieldType, fieldName, featuresAll);
        }

        jsonReader.nextIfMatch('{');
        T object = creator != null
                ? creator.get()
                : null;

        if (hasDefaultValue) {
            initDefaultValue(object);
        }

        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfMatch('}')) {
                break;
            }

            long hashCode = jsonReader.readFieldNameHashCode();
            if (i == 0 && hashCode == HASH_TYPE) {
                long typeHash = jsonReader.readTypeHashCode();
                JSONReader.Context context = jsonReader.getContext();
                ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
                if (autoTypeObjectReader == null) {
                    String typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, objectClass);

                    if (autoTypeObjectReader == null) {
                        continue;
                    }
                }

                if (autoTypeObjectReader != this) {
                    object = (T) autoTypeObjectReader.readObject(jsonReader, fieldType, fieldName, features);
                } else {
                    continue;
                }
                break;
            }

            if (hashCode == this.hashCode0) {
                fieldReader0.readFieldValue(jsonReader, object);
            } else {
                if (jsonReader.isSupportSmartMatch(features | this.features)
                        && jsonReader.getNameHashCodeLCase() == this.hashCode0LCase) {
                    fieldReader0.readFieldValue(jsonReader, object);
                } else {
                    processExtra(jsonReader, object);
                }
            }
        }

        jsonReader.nextIfMatch(',');

        if (buildFunction != null) {
            object = (T) buildFunction.apply(object);
        }

        if (schema != null) {
            schema.assertValidate(object);
        }

        return object;
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        if (hashCode == this.hashCode0) {
            return fieldReader0;
        }

        return null;
    }

    @Override
    public FieldReader getFieldReaderLCase(long hashCode) {
        if (hashCode == this.hashCode0LCase) {
            return fieldReader0;
        }

        return null;
    }

    @Override
    public boolean setFieldValue(Object object, String fieldName, long fieldNameHashCode, int value) {
        if (this.hashCode0 != fieldNameHashCode) {
            return false;
        }
        fieldReader0.accept(object, value);
        return true;
    }
}
