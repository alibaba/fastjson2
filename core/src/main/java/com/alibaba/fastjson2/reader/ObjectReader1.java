package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT;
import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT_END;

final class ObjectReader1<T>
        extends ObjectReaderBean<T> {
    final long features;
    final Supplier<T> defaultCreator;
    final Function buildFunction;
    final FieldReader fieldReader;

    final long hashCode;
    final long hashCodeLCase;

    ObjectReader1(
            Class objectClass,
            long features,
            JSONSchema schema,
            Supplier<T> defaultCreator,
            Function buildFunction,
            FieldReader fieldReader) {
        super(objectClass, null, schema);

        this.features = features;
        this.defaultCreator = defaultCreator;
        this.buildFunction = buildFunction;
        this.fieldReader = fieldReader;
        this.hashCode = fieldReader.fieldNameHash;
        this.hashCodeLCase = fieldReader.fieldNameHashLCase;

        if (fieldReader.isUnwrapped()) {
            extraFieldReader = fieldReader;
        }

        hasDefaultValue = fieldReader.defaultValue != null;
    }

    @Override
    public long getFeatures() {
        return features;
    }

    @Override
    public Function getBuildFunction() {
        return buildFunction;
    }

    @Override
    public T createInstance(long features) {
        return defaultCreator.get();
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

        jsonReader.startArray();
        Object object = defaultCreator.get();

        fieldReader.readFieldValue(jsonReader, object);

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
            int entryCnt = jsonReader.startArray();
            if (entryCnt != 1) {
                throw new JSONException(jsonReader.info("not support input entryCount " + entryCnt));
            }

            Object object = defaultCreator.get();
            fieldReader.readFieldValue(jsonReader, object);

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
        if (defaultCreator != null) {
            object = defaultCreator.get();
        } else if (JDKUtils.UNSAFE_SUPPORT && ((features | jsonReader.getContext().getFeatures()) & JSONReader.Feature.FieldBased.mask) != 0) {
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

            if (hashCode == this.hashCode) {
                fieldReader.readFieldValueJSONB(jsonReader, object);
            } else {
                if (jsonReader.isSupportSmartMatch(features | this.features)
                        && jsonReader.getNameHashCodeLCase() == this.hashCodeLCase) {
                    fieldReader.readFieldValue(jsonReader, object);
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
        fieldReader.setDefault(object);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, 0);
        }

        if (jsonReader.nextIfNull() || jsonReader.nextIfEmptyString()) {
            return null;
        }

        long featuresAll = jsonReader.features(this.features | features);
        if (jsonReader.isArray()) {
            if ((featuresAll & JSONReader.Feature.SupportArrayToBean.mask) != 0) {
                jsonReader.next();
                Object object = defaultCreator.get();

                fieldReader.readFieldValue(jsonReader, object);
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
        T object = defaultCreator != null
                ? defaultCreator.get()
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

            if (hashCode == this.hashCode) {
                fieldReader.readFieldValue(jsonReader, object);
            } else {
                if (jsonReader.isSupportSmartMatch(features | this.features)
                        && jsonReader.getNameHashCodeLCase() == this.hashCodeLCase) {
                    fieldReader.readFieldValue(jsonReader, object);
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
        if (hashCode == this.hashCode) {
            return fieldReader;
        }

        return null;
    }

    @Override
    public FieldReader getFieldReaderLCase(long hashCode) {
        if (hashCode == this.hashCodeLCase) {
            return fieldReader;
        }

        return null;
    }

    @Override
    public boolean setFieldValue(Object object, String fieldName, long fieldNameHashCode, int value) {
        if (this.hashCode != fieldNameHashCode) {
            return false;
        }
        fieldReader.accept(object, value);
        return true;
    }
}
