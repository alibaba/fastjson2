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

final class ObjectReader1<T> extends ObjectReaderBean<T> {
    final long features;
    final Supplier<T> defaultCreator;
    final Function buildFunction;
    final FieldReader fieldReader;

    final long hashCode;
    final long hashCodeLCase;

    ObjectReader1(Class objectClass, long features, Supplier<T> defaultCreator, Function buildFunction, FieldReader fieldReader) {
        super(objectClass, null);

        this.features = features;
        this.defaultCreator = defaultCreator;
        this.buildFunction = buildFunction;
        this.fieldReader = fieldReader;
        this.hashCode = Fnv.hashCode64(fieldReader.getFieldName());
        this.hashCodeLCase = Fnv.hashCode64LCase(fieldReader.getFieldName());
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
        return readObject(jsonReader, features);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, long features) {
        ObjectReader autoTypeReader = checkAutoType(jsonReader, this.objectClass, this.features | features);
        if (autoTypeReader != null && autoTypeReader != this && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, features);
        }

        if (jsonReader.isArray()) {
            int entryCnt = jsonReader.startArray();
            if (entryCnt != 1) {
                throw new JSONException("not support input entryCount " + entryCnt);
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
                throw new JSONException("expect object, but " + JSONB.typeName(jsonReader.getType()));
            }
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
                        throw new JSONException("auotype not support : " + typeName);
                    }
                }

                if (autoTypeObjectReader == this) {
                    continue;
                }

                return (T) autoTypeObjectReader.readJSONBObject(jsonReader, features);
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
            return readJSONBObject(jsonReader, 0);
        }

        if (jsonReader.nextIfNull()) {
            jsonReader.nextIfMatch(',');
            return null;
        }

        if (jsonReader.isArray()
                && jsonReader.isSupportBeanArray(features | getFeatures())) {
            jsonReader.nextIfMatch('[');
            Object object = defaultCreator.get();

            fieldReader.readFieldValue(jsonReader, object);
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
        Object object = defaultCreator != null
                ? defaultCreator.get()
                : null;

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
