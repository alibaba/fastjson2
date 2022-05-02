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

final class ObjectReader6<T> extends ObjectReaderBean<T> {
    final Supplier<T> defaultCreator;
    final long features;
    final Function buildFunction;
    final FieldReader fieldReader0;
    final FieldReader fieldReader1;
    final FieldReader fieldReader2;
    final FieldReader fieldReader3;
    final FieldReader fieldReader4;
    final FieldReader fieldReader5;
    final long hashCode0;
    final long hashCode1;
    final long hashCode2;
    final long hashCode3;
    final long hashCode4;
    final long hashCode5;
    final long hashCode0LCase;
    final long hashCode1LCase;
    final long hashCode2LCase;
    final long hashCode3LCase;
    final long hashCode4LCase;
    final long hashCode5LCase;

    ObjectReader6(
            Class objectClass
            , Supplier<T> defaultCreator
            , long features
            , Function buildFunction
            , FieldReader fieldReader0
            , FieldReader fieldReader1
            , FieldReader fieldReader2
            , FieldReader fieldReader3
            , FieldReader fieldReader4
            , FieldReader fieldReader5
    ) {
        super(objectClass, null);

        this.defaultCreator = defaultCreator;
        this.features = features;
        this.buildFunction = buildFunction;
        this.fieldReader0 = fieldReader0;
        this.fieldReader1 = fieldReader1;
        this.fieldReader2 = fieldReader2;
        this.fieldReader3 = fieldReader3;
        this.fieldReader4 = fieldReader4;
        this.fieldReader5 = fieldReader5;

        String fieldName0 = fieldReader0.getFieldName();
        String fieldName1 = fieldReader1.getFieldName();
        String fieldName2 = fieldReader2.getFieldName();
        String fieldName3 = fieldReader3.getFieldName();
        String fieldName4 = fieldReader4.getFieldName();
        String fieldName5 = fieldReader5.getFieldName();

        this.hashCode0 = Fnv.hashCode64(fieldName0);
        this.hashCode1 = Fnv.hashCode64(fieldName1);
        this.hashCode2 = Fnv.hashCode64(fieldName2);
        this.hashCode3 = Fnv.hashCode64(fieldName3);
        this.hashCode4 = Fnv.hashCode64(fieldName4);
        this.hashCode5 = Fnv.hashCode64(fieldName5);

        this.hashCode0LCase = Fnv.hashCode64LCase(fieldName0);
        this.hashCode1LCase = Fnv.hashCode64LCase(fieldName1);
        this.hashCode2LCase = Fnv.hashCode64LCase(fieldName2);
        this.hashCode3LCase = Fnv.hashCode64LCase(fieldName3);
        this.hashCode4LCase = Fnv.hashCode64LCase(fieldName4);
        this.hashCode5LCase = Fnv.hashCode64LCase(fieldName5);
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
            fieldReader4.readFieldValue(jsonReader, object);
            fieldReader5.readFieldValue(jsonReader, object);

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
            } else if (hashCode == hashCode3) {
                fieldReader3.readFieldValue(jsonReader, object);
            } else if (hashCode == hashCode4) {
                fieldReader4.readFieldValue(jsonReader, object);
            } else if (hashCode == hashCode5) {
                fieldReader5.readFieldValue(jsonReader, object);
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
                } else if (nameHashCodeLCase == hashCode3LCase) {
                    fieldReader3.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode4LCase) {
                    fieldReader4.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode5LCase) {
                    fieldReader5.readFieldValue(jsonReader, object);
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
            fieldReader3.readFieldValue(jsonReader, object);
            fieldReader4.readFieldValue(jsonReader, object);
            fieldReader5.readFieldValue(jsonReader, object);
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
//                jsonReader.next();
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
            } else if (hashCode == hashCode4) {
                fieldReader4.readFieldValue(jsonReader, object);
            } else if (hashCode == hashCode5) {
                fieldReader5.readFieldValue(jsonReader, object);
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
                } else if (nameHashCodeLCase == hashCode3LCase) {
                    fieldReader3.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode4LCase) {
                    fieldReader4.readFieldValue(jsonReader, object);
                } else if (nameHashCodeLCase == hashCode5LCase) {
                    fieldReader5.readFieldValue(jsonReader, object);
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

        if (hashCode == hashCode3) {
            return fieldReader3;
        }

        if (hashCode == hashCode4) {
            return fieldReader4;
        }

        if (hashCode == hashCode5) {
            return fieldReader5;
        }

        return null;
    }
}
