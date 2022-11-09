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
import static com.alibaba.fastjson2.JSONReader.Feature.SupportArrayToBean;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE_SUPPORT;

public class ObjectReader2<T>
        extends ObjectReaderAdapter<T> {
    protected final FieldReader fieldReader0;
    protected final FieldReader fieldReader1;
    protected final long hashCode0;
    protected final long hashCode1;
    protected final long hashCode0LCase;
    protected final long hashCode1LCase;

    protected ObjectReader objectReader0;
    protected ObjectReader objectReader1;

    public ObjectReader2(
            Class objectClass,
            long features,
            JSONSchema schema,
            Supplier<T> creator,
            Function buildFunction,
            FieldReader first,
            FieldReader second
    ) {
        this(objectClass, null, null, features, schema, creator, buildFunction, first, second);
    }

    public ObjectReader2(
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
        this.fieldReader1 = fieldReaders[1];

        this.hashCode0 = fieldReader0.fieldNameHash;
        this.hashCode0LCase = fieldReader0.fieldNameHashLCase;

        this.hashCode1 = fieldReader1.fieldNameHash;
        this.hashCode1LCase = fieldReader1.fieldNameHashLCase;

        if (fieldReader0.isUnwrapped()) {
            extraFieldReader = fieldReader0;
        }
        if (fieldReader1.isUnwrapped()) {
            extraFieldReader = fieldReader1;
        }

        hasDefaultValue = fieldReader0.defaultValue != null || fieldReader1.defaultValue != null;
    }

    @Override
    protected void initDefaultValue(T object) {
        fieldReader0.acceptDefaultValue(object);
        fieldReader1.acceptDefaultValue(object);
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
            if (entryCnt > 1) {
                fieldReader1.readFieldValue(jsonReader, object);
                for (int i = 2; i < entryCnt; ++i) {
                    jsonReader.skipValue();
                }
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

        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (jsonReader.isArray()) {
            T object = creator.get();
            if (hasDefaultValue) {
                initDefaultValue(object);
            }

            int entryCnt = jsonReader.startArray();
            if (entryCnt > 0) {
                fieldReader0.readFieldValue(jsonReader, object);
                if (entryCnt > 1) {
                    fieldReader1.readFieldValue(jsonReader, object);
                    for (int i = 2; i < entryCnt; ++i) {
                        jsonReader.skipValue();
                    }
                }
            }

            if (buildFunction != null) {
                return (T) buildFunction.apply(object);
            }
            return (T) object;
        }

        if (!jsonReader.nextIfMatch(BC_OBJECT)) {
            throw new JSONException(jsonReader.info("expect object, but " + JSONB.typeName(jsonReader.getType())));
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

        for (; ; ) {
            if (jsonReader.nextIfMatch(BC_OBJECT_END)) {
                break;
            }

            long hashCode = jsonReader.readFieldNameHashCode();
            if (hashCode == 0) {
                continue;
            }

            if (hashCode == hashCode0) {
                fieldReader0.readFieldValue(jsonReader, object);
            } else if (hashCode == hashCode1) {
                fieldReader1.readFieldValueJSONB(jsonReader, object);
            } else {
                if (jsonReader.isSupportSmartMatch(features | this.features)) {
                    long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                    if (nameHashCodeLCase == hashCode0LCase) {
                        fieldReader0.readFieldValueJSONB(jsonReader, object);
                        continue;
                    } else if (nameHashCodeLCase == hashCode1LCase) {
                        fieldReader1.readFieldValueJSONB(jsonReader, object);
                        continue;
                    }
                }

                processExtra(jsonReader, object);
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
    public T readObject(JSONReader jsonReader) {
        return readObject(jsonReader, null, null, features);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (jsonReader.nextIfNull()) {
            jsonReader.nextIfMatch(',');
            return null;
        }

        long featuresAll = jsonReader.features(this.features | features);
        T object;
        if (jsonReader.isArray()) {
            if ((featuresAll & SupportArrayToBean.mask) != 0) {
                jsonReader.next();
                object = creator.get();
                if (hasDefaultValue) {
                    initDefaultValue(object);
                }

                fieldReader0.readFieldValue(jsonReader, object);
                fieldReader1.readFieldValue(jsonReader, object);
                if (jsonReader.current() != ']') {
                    throw new JSONException(jsonReader.info("array to bean end error"));
                }
                jsonReader.next();
                return object;
            }

            return processObjectInputSingleItemArray(jsonReader, fieldType, fieldName, featuresAll);
        }

        jsonReader.nextIfMatch('{');
        object = creator.get();
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
                    break;
                } else {
                    continue;
                }
            }

            if (hashCode == hashCode0) {
                fieldReader0.readFieldValue(jsonReader, object);
            } else if (hashCode == hashCode1) {
                fieldReader1.readFieldValue(jsonReader, object);
            } else {
                if (jsonReader.isSupportSmartMatch(features | this.features)) {
                    long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                    if (nameHashCodeLCase == hashCode0LCase) {
                        fieldReader0.readFieldValue(jsonReader, object);
                        continue;
                    } else if (nameHashCodeLCase == hashCode1LCase) {
                        fieldReader1.readFieldValue(jsonReader, object);
                        continue;
                    }
                }

                processExtra(jsonReader, object);
            }
        }

        jsonReader.nextIfMatch(',');

        if (buildFunction != null) {
            try {
                object = (T) buildFunction.apply(object);
            } catch (IllegalStateException e) {
                throw new JSONException(jsonReader.info("build object error"), e);
            }
        }

        if (schema != null) {
            schema.assertValidate(object);
        }

        return object;
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        if (hashCode == hashCode0) {
            return fieldReader0;
        }

        if (hashCode == hashCode1) {
            return fieldReader1;
        }

        return null;
    }

    @Override
    public FieldReader getFieldReaderLCase(long hashCode) {
        if (hashCode == hashCode0LCase) {
            return fieldReader0;
        }

        if (hashCode == hashCode1LCase) {
            return fieldReader1;
        }

        return null;
    }
}
