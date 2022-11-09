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

public class ObjectReader4<T>
        extends ObjectReaderAdapter<T> {
    protected final FieldReader fieldReader0;
    protected final FieldReader fieldReader1;
    protected final FieldReader fieldReader2;
    protected final FieldReader fieldReader3;

    final long hashCode0;
    final long hashCode1;
    final long hashCode2;
    final long hashCode3;

    final long hashCode0LCase;
    final long hashCode1LCase;
    final long hashCode2LCase;
    final long hashCode3LCase;

    protected ObjectReader objectReader0;
    protected ObjectReader objectReader1;
    protected ObjectReader objectReader2;
    protected ObjectReader objectReader3;

    ObjectReader4(
            Class objectClass,
            long features,
            JSONSchema schema,
            Supplier<T> creator,
            Function buildFunction,
            FieldReader fieldReader0,
            FieldReader fieldReader1,
            FieldReader fieldReader2,
            FieldReader fieldReader3
    ) {
        this(
                objectClass,
                null,
                null,
                features,
                schema,
                creator,
                buildFunction,
                fieldReader0,
                fieldReader1,
                fieldReader2,
                fieldReader3
        );
    }

    public ObjectReader4(
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
        this.fieldReader2 = fieldReaders[2];
        this.fieldReader3 = fieldReaders[3];

        this.hashCode0 = fieldReader0.fieldNameHash;
        this.hashCode1 = fieldReader1.fieldNameHash;
        this.hashCode2 = fieldReader2.fieldNameHash;
        this.hashCode3 = fieldReader3.fieldNameHash;

        this.hashCode0LCase = fieldReader0.fieldNameHashLCase;
        this.hashCode1LCase = fieldReader1.fieldNameHashLCase;
        this.hashCode2LCase = fieldReader2.fieldNameHashLCase;
        this.hashCode3LCase = fieldReader3.fieldNameHashLCase;

        if (fieldReader0.isUnwrapped()) {
            extraFieldReader = fieldReader0;
        }
        if (fieldReader1.isUnwrapped()) {
            extraFieldReader = fieldReader1;
        }
        if (fieldReader2.isUnwrapped()) {
            extraFieldReader = fieldReader2;
        }
        if (fieldReader3.isUnwrapped()) {
            extraFieldReader = fieldReader3;
        }

        hasDefaultValue = fieldReader0.defaultValue != null
                || fieldReader1.defaultValue != null
                || fieldReader2.defaultValue != null
                || fieldReader3.defaultValue != null;
    }

    @Override
    protected void initDefaultValue(T object) {
        fieldReader0.acceptDefaultValue(object);
        fieldReader1.acceptDefaultValue(object);
        fieldReader2.acceptDefaultValue(object);
        fieldReader3.acceptDefaultValue(object);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.isArray()) {
            Object object = creator.get();

            int entryCnt = jsonReader.startArray();
            if (entryCnt > 0) {
                fieldReader0.readFieldValue(jsonReader, object);
                if (entryCnt > 1) {
                    fieldReader1.readFieldValue(jsonReader, object);
                    if (entryCnt > 2) {
                        fieldReader2.readFieldValue(jsonReader, object);
                        if (entryCnt > 3) {
                            fieldReader3.readFieldValue(jsonReader, object);

                            for (int i = 4; i < entryCnt; ++i) {
                                jsonReader.skipValue();
                            }
                        }
                    }
                }
            }

            if (buildFunction != null) {
                return (T) buildFunction.apply(object);
            }
            return (T) object;
        }

        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
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
                fieldReader1.readFieldValue(jsonReader, object);
            } else if (hashCode == hashCode2) {
                fieldReader2.readFieldValue(jsonReader, object);
            } else if (hashCode == hashCode3) {
                fieldReader3.readFieldValue(jsonReader, object);
            } else {
                if (!jsonReader.isSupportSmartMatch(features | this.features)) {
                    processExtra(jsonReader, object);
                    continue;
                }

                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                if (nameHashCodeLCase == hashCode0LCase) {
                    fieldReader0.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode1LCase) {
                    fieldReader1.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode2LCase) {
                    fieldReader2.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode3LCase) {
                    fieldReader3.readFieldValue(jsonReader, object);
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
    public T readArrayMappingJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        ObjectReader autoTypeReader = checkAutoType(jsonReader, this.objectClass, this.features | features);
        if (autoTypeReader != null && autoTypeReader != this && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readArrayMappingJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        int entryCnt = jsonReader.startArray();
        Object object = creator.get();

        if (entryCnt > 0) {
            fieldReader0.readFieldValue(jsonReader, object);
            if (entryCnt > 1) {
                fieldReader1.readFieldValue(jsonReader, object);
                if (entryCnt > 2) {
                    fieldReader2.readFieldValue(jsonReader, object);
                    if (entryCnt > 3) {
                        fieldReader3.readFieldValue(jsonReader, object);

                        for (int i = 4; i < entryCnt; ++i) {
                            jsonReader.skipValue();
                        }
                    }
                }
            }
        }

        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return (T) object;
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
        if (jsonReader.isArray()) {
            if ((featuresAll & JSONReader.Feature.SupportArrayToBean.mask) != 0) {
                jsonReader.nextIfMatch('[');
                T object = creator.get();
                if (hasDefaultValue) {
                    initDefaultValue(object);
                }

                fieldReader0.readFieldValue(jsonReader, object);
                fieldReader1.readFieldValue(jsonReader, object);
                fieldReader2.readFieldValue(jsonReader, object);
                fieldReader3.readFieldValue(jsonReader, object);
                if (!jsonReader.nextIfMatch(']')) {
                    throw new JSONException(jsonReader.info("array to bean end error"));
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
        T object = creator.get();
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
            } else if (hashCode == hashCode2) {
                fieldReader2.readFieldValue(jsonReader, object);
            } else if (hashCode == hashCode3) {
                fieldReader3.readFieldValue(jsonReader, object);
            } else {
                if (!jsonReader.isSupportSmartMatch(features | this.features)) {
                    processExtra(jsonReader, object);
                    continue;
                }

                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                if (nameHashCodeLCase == hashCode0LCase) {
                    fieldReader0.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode1LCase) {
                    fieldReader1.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode2LCase) {
                    fieldReader2.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode3LCase) {
                    fieldReader3.readFieldValue(jsonReader, object);
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
        if (hashCode == hashCode0) {
            return fieldReader0;
        }

        if (hashCode == hashCode1) {
            return fieldReader1;
        }

        if (hashCode == hashCode2) {
            return fieldReader2;
        }

        if (hashCode == hashCode3) {
            return fieldReader3;
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

        if (hashCode == hashCode2LCase) {
            return fieldReader2;
        }

        if (hashCode == hashCode3LCase) {
            return fieldReader3;
        }

        return null;
    }
}
