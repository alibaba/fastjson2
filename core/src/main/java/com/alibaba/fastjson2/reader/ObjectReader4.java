package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT;
import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT_END;

final class ObjectReader4<T>
        extends ObjectReaderBean<T> {
    final long features;
    final Supplier<T> defaultCreator;
    final Function buildFunction;
    final FieldReader fieldReader0;
    final FieldReader fieldReader1;
    final FieldReader fieldReader2;
    final FieldReader fieldReader3;
    final long hashCode0;
    final long hashCode1;
    final long hashCode2;
    final long hashCode3;

    final long hashCode0LCase;
    final long hashCode1LCase;
    final long hashCode2LCase;
    final long hashCode3LCase;

    ObjectReader4(
            Class objectClass,
            long features,
            JSONSchema schema,
            Supplier<T> defaultCreator,
            Function buildFunction,
            FieldReader fieldReader0,
            FieldReader fieldReader1,
            FieldReader fieldReader2,
            FieldReader fieldReader3
    ) {
        super(objectClass, null, schema);

        this.features = features;
        this.defaultCreator = defaultCreator;
        this.buildFunction = buildFunction;
        this.fieldReader0 = fieldReader0;
        this.fieldReader1 = fieldReader1;
        this.fieldReader2 = fieldReader2;
        this.fieldReader3 = fieldReader3;

        String fieldName0 = fieldReader0.getFieldName();
        String fieldName1 = fieldReader1.getFieldName();
        String fieldName2 = fieldReader2.getFieldName();
        String fieldName3 = fieldReader3.getFieldName();

        this.hashCode0 = Fnv.hashCode64(fieldName0);
        this.hashCode1 = Fnv.hashCode64(fieldName1);
        this.hashCode2 = Fnv.hashCode64(fieldName2);
        this.hashCode3 = Fnv.hashCode64(fieldName3);

        this.hashCode0LCase = Fnv.hashCode64LCase(fieldName0);
        this.hashCode1LCase = Fnv.hashCode64LCase(fieldName1);
        this.hashCode2LCase = Fnv.hashCode64LCase(fieldName2);
        this.hashCode3LCase = Fnv.hashCode64LCase(fieldName3);

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

        hasDefaultValue = fieldReader0.getDefaultValue() != null
                || fieldReader1.getDefaultValue() != null
                || fieldReader2.getDefaultValue() != null
                || fieldReader3.getDefaultValue() != null;
    }

    @Override
    protected void initDefaultValue(T object) {
        fieldReader0.setDefault(object);
        fieldReader1.setDefault(object);
        fieldReader2.setDefault(object);
        fieldReader3.setDefault(object);
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
    public T readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.isArray()) {
            int entryCnt = jsonReader.startArray();
            Object object = defaultCreator.get();

            fieldReader0.readFieldValue(jsonReader, object);
            fieldReader1.readFieldValue(jsonReader, object);
            fieldReader2.readFieldValue(jsonReader, object);
            fieldReader3.readFieldValue(jsonReader, object);

            if (buildFunction != null) {
                return (T) buildFunction.apply(object);
            }
            return (T) object;
        }

        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, features);
        }

        if (!jsonReader.nextIfMatch(BC_OBJECT)) {
            throw new JSONException("expect object, but " + JSONB.typeName(jsonReader.getType()));
        }

        T object;
        if (defaultCreator != null) {
            object = defaultCreator.get();
        } else if (JDKUtils.UNSAFE_SUPPORT && ((features | jsonReader.getContext().getFeatures()) & JSONReader.Feature.FieldBased.mask) != 0) {
            try {
                object = (T) UnsafeUtils.UNSAFE.allocateInstance(objectClass);
            } catch (InstantiationException e) {
                throw new JSONException("create instance error", e);
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
    public T readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, features);
        }

        if (jsonReader.nextIfNull()) {
            jsonReader.nextIfMatch(',');
            return null;
        }

        if (jsonReader.isArray()
                && jsonReader.isSupportBeanArray()) {
            jsonReader.nextIfMatch('[');
            T object = defaultCreator.get();
            if (hasDefaultValue) {
                initDefaultValue(object);
            }

            fieldReader0.readFieldValue(jsonReader, object);
            fieldReader1.readFieldValue(jsonReader, object);
            fieldReader2.readFieldValue(jsonReader, object);
            fieldReader3.readFieldValue(jsonReader, object);
            if (!jsonReader.nextIfMatch(']')) {
                throw new JSONException("array to bean end error, " + jsonReader.current());
            }

            jsonReader.nextIfMatch(',');

            if (buildFunction != null) {
                return (T) buildFunction.apply(object);
            }
            return (T) object;
        }

        jsonReader.nextIfMatch('{');
        T object = defaultCreator.get();
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
                    object = (T) autoTypeObjectReader.readObject(jsonReader, features);
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
