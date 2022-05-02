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

public class ObjectReader2<T> extends ObjectReaderBean<T> {
    final long features;
    final Supplier<T> defaultCreator;
    final Function buildFunction;
    private final FieldReader first;
    private final FieldReader second;
    private final long firstHashCode;
    private final long secondHashCode;
    private final long firstHashCodeLCase;
    private final long secondHashCodeLCase;

    public ObjectReader2(
            Class objectClass
            , long features
            , Supplier<T> defaultCreator
            , Function buildFunction
            , FieldReader first
            , FieldReader second) {
        super(objectClass, null);

        this.features = features;
        this.defaultCreator = defaultCreator;
        this.buildFunction = buildFunction;

        this.first = first;
        this.second = second;

        String fieldName0 = first.getFieldName();
        this.firstHashCode = Fnv.hashCode64(fieldName0);
        this.firstHashCodeLCase = Fnv.hashCode64LCase(fieldName0);

        String fieldName1 = second.getFieldName();
        this.secondHashCode = Fnv.hashCode64(fieldName1);
        this.secondHashCodeLCase = Fnv.hashCode64LCase(fieldName1);
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
        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, features);
        }

        if (jsonReader.isArray()) {
            int entryCnt = jsonReader.startArray();
            if (entryCnt != 2) {
                throw new JSONException("not support input entryCount " + entryCnt);
            }

            Object object = defaultCreator.get();

            first.readFieldValue(jsonReader, object);
            second.readFieldValue(jsonReader, object);

            if (buildFunction != null) {
                return (T) buildFunction.apply(object);
            }
            return (T) object;
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

            if (hashCode == firstHashCode) {
                first.readFieldValue(jsonReader, object);
            } else if (hashCode == secondHashCode) {
                second.readFieldValueJSONB(jsonReader, object);
            } else {
                if (jsonReader.isSupportSmartMatch(features | this.features)) {
                    long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                    if (nameHashCodeLCase == firstHashCodeLCase) {
                        first.readFieldValueJSONB(jsonReader, object);
                        continue;
                    } else if (nameHashCodeLCase == secondHashCodeLCase) {
                        second.readFieldValueJSONB(jsonReader, object);
                        continue;
                    }
                }

                jsonReader.skipValue();
            }
        }

        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return (T) object;
    }

    @Override
    public T readObject(JSONReader jsonReader) {
        return readObject(jsonReader, features);
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

        Object object;
        if (jsonReader.isArray()
                && jsonReader.isSupportBeanArray(this.features | features)) {
            jsonReader.next();
            object = defaultCreator.get();

            first.readFieldValue(jsonReader, object);
            second.readFieldValue(jsonReader, object);
            if (jsonReader.current() != ']') {
                throw new JSONException("array to bean end error, " + jsonReader.current());
            }
            jsonReader.next();

        } else {
            jsonReader.nextIfMatch('{');
            object = defaultCreator.get();
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

                if (hashCode == firstHashCode) {
                    first.readFieldValue(jsonReader, object);
                } else if (hashCode == secondHashCode) {
                    second.readFieldValue(jsonReader, object);
                } else {
                    if (jsonReader.isSupportSmartMatch(features | this.features)) {
                        long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                        if (nameHashCodeLCase == firstHashCodeLCase) {
                            first.readFieldValue(jsonReader, object);
                            continue;
                        } else if (nameHashCodeLCase == secondHashCodeLCase) {
                            second.readFieldValue(jsonReader, object);
                            continue;
                        }
                    }
                    jsonReader.skipValue();
                }
            }
        }

        jsonReader.nextIfMatch(',');

        if (buildFunction != null) {
            try {
                return (T) buildFunction.apply(object);
            } catch (IllegalStateException e) {
                throw new JSONException("build object error", e);
            }
        }

        return (T) object;
    }

    @Override
    public FieldReader getFieldReader(long hashCode) {
        if (hashCode == firstHashCode) {
            return first;
        }

        if (hashCode == secondHashCode) {
            return second;
        }

        return null;
    }
}
