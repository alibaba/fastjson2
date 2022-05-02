package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT;
import static com.alibaba.fastjson2.JSONB.Constants.BC_OBJECT_END;

class ObjectReader3<T> extends ObjectReaderBean<T> {
    final Supplier<T> defaultCreator;
    final long features;
    final Function buildFunction;
    final FieldReader fieldReader0;
    final FieldReader fieldReader1;
    final FieldReader fieldReader2;
    final long hashCode0;
    final long hashCode1;
    final long hashCode2;

    final long hashCode0LCase;
    final long hashCode1LCase;
    final long hashCode2LCase;

    ObjectReader3(
            Class objectClass
            , Supplier<T> defaultCreator
            , long features
            , Function buildFunction
            , FieldReader fieldReader0
            , FieldReader fieldReader1
            , FieldReader fieldReader2
    ) {
        super(objectClass, null);

        this.defaultCreator = defaultCreator;
        this.features = features;
        this.buildFunction = buildFunction;
        this.fieldReader0 = fieldReader0;
        this.fieldReader1 = fieldReader1;
        this.fieldReader2 = fieldReader2;

        String fieldName0 = fieldReader0.getFieldName();
        String fieldName1 = fieldReader1.getFieldName();
        String fieldName2 = fieldReader2.getFieldName();

        this.hashCode0 = Fnv.hashCode64(fieldName0);
        this.hashCode1 = Fnv.hashCode64(fieldName1);
        this.hashCode2 = Fnv.hashCode64(fieldName2);

        this.hashCode0LCase = Fnv.hashCode64LCase(fieldName0);
        this.hashCode1LCase = Fnv.hashCode64LCase(fieldName1);
        this.hashCode2LCase = Fnv.hashCode64LCase(fieldName2);
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

        Object object;
        if (defaultCreator != null) {
            object = defaultCreator.get();
        } else if (JDKUtils.UNSAFE_SUPPORT && ((features | jsonReader.getContext().getFeatures()) & JSONReader.Feature.FieldBased.mask) != 0) {
            try {
                object = UnsafeUtils.UNSAFE.allocateInstance(objectClass);
            } catch (InstantiationException e) {
                throw new JSONException("create instance error", e);
            }
        } else {
            object = null;
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
            } else {
                if (!jsonReader.isSupportSmartMatch(features | this.features)) {
                    jsonReader.skipValue();
                    continue;
                }
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                if (nameHashCodeLCase == hashCode0) {
                    fieldReader0.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode1) {
                    fieldReader1.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode2) {
                    fieldReader2.readFieldValue(jsonReader, object);
                } else {
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
            Object object = defaultCreator.get();

            fieldReader0.readFieldValue(jsonReader, object);
            fieldReader1.readFieldValue(jsonReader, object);
            fieldReader2.readFieldValue(jsonReader, object);
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
        Object object = defaultCreator.get();
        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfMatch('}')) {
                break;
            }

            long hashCode = jsonReader.readFieldNameHashCode();
            if (hashCode == 0) {
                continue;
            }

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
            } else {
                if (!jsonReader.isSupportSmartMatch(features | this.features)) {
                    jsonReader.skipValue();
                    continue;
                }

                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                if (nameHashCodeLCase == hashCode0LCase) {
                    fieldReader0.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode1LCase) {
                    fieldReader1.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode2LCase) {
                    fieldReader2.readFieldValue(jsonReader, object);
                } else {
                    jsonReader.skipValue();
                }
            }
        }

        jsonReader.nextIfMatch(',');

        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }
        return (T) object;
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

        return null;
    }
}
