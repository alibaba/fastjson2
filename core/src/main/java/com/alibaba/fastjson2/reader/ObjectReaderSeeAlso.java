package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

final class ObjectReaderSeeAlso<T>
        extends ObjectReaderAdapter<T> {
    final Class[] seeAlso;
    final String[] seeAlsoNames;
    final Map<Long, Class> seeAlsoMapping;

    ObjectReaderSeeAlso(
            Class objectType,
            Supplier<T> defaultCreator,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            FieldReader... fieldReaders
    ) {
        super(objectType, typeKey, null, JSONReader.Feature.SupportAutoType.mask, null, defaultCreator, null, fieldReaders);
        this.seeAlso = seeAlso;
        seeAlsoMapping = new HashMap<>(seeAlso.length);
        this.seeAlsoNames = new String[seeAlso.length];
        for (int i = 0; i < seeAlso.length; i++) {
            Class seeAlsoClass = seeAlso[i];

            String typeName = null;
            if (seeAlsoNames != null && seeAlsoNames.length >= i + 1) {
                typeName = seeAlsoNames[i];
            }
            if (typeName == null || typeName.isEmpty()) {
                typeName = seeAlsoClass.getSimpleName();
            }
            long hashCode = Fnv.hashCode64(typeName);
            seeAlsoMapping.put(hashCode, seeAlsoClass);
            this.seeAlsoNames[i] = typeName;
        }
    }

    @Override
    public T createInstance(long features) {
        if (creator == null) {
            return null;
        }
        return creator.get();
    }

    @Override
    public ObjectReader autoType(JSONReader.Context context, long typeHash) {
        Class seeAlsoClass = seeAlsoMapping.get(typeHash);
        if (seeAlsoClass == null) {
            return null;
        }

        return context.getObjectReader(seeAlsoClass);
    }

    @Override
    public ObjectReader autoType(ObjectReaderProvider provider, long typeHash) {
        Class seeAlsoClass = seeAlsoMapping.get(typeHash);
        if (seeAlsoClass == null) {
            return null;
        }

        return provider.getObjectReader(seeAlsoClass);
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.nextIfNull()) {
            jsonReader.nextIfMatch(',');
            return null;
        }

        if (jsonReader.isString()) {
            long valueHashCode = jsonReader.readValueHashCode();

            for (Class seeAlsoType : seeAlso) {
                if (Enum.class.isAssignableFrom(seeAlsoType)) {
                    ObjectReader seeAlsoTypeReader = jsonReader.getObjectReader(seeAlsoType);

                    Enum e = null;
                    if (seeAlsoTypeReader instanceof ObjectReaderImplEnum) {
                        e = ((ObjectReaderImplEnum) seeAlsoTypeReader).getEnumByHashCode(valueHashCode);
                    } else if (seeAlsoTypeReader instanceof ObjectReaderImplEnum2X4) {
                        e = ((ObjectReaderImplEnum2X4) seeAlsoTypeReader).getEnumByHashCode(valueHashCode);
                    }

                    if (e != null) {
                        return (T) e;
                    }
                }
            }

            String strVal = jsonReader.getString();
            throw new JSONException(jsonReader.info("not support input " + strVal));
        }

        JSONReader.SavePoint savePoint = jsonReader.mark();

        long featuresAll = jsonReader.features(this.getFeatures() | features);
        if (jsonReader.isArray()) {
            if ((featuresAll & JSONReader.Feature.SupportArrayToBean.mask) != 0) {
                return readArrayMappingObject(jsonReader, fieldType, fieldName, features);
            }

            return processObjectInputSingleItemArray(jsonReader, fieldType, fieldName, featuresAll);
        }

        T object = null;
        boolean objectStart = jsonReader.nextIfMatch('{');
        if (!objectStart) {
            char ch = jsonReader.current();
            // skip for fastjson 1.x compatible
            if (ch == 't' || ch == 'f') {
                jsonReader.readBoolValue(); // skip
                return null;
            }

            if (ch != '"' && ch != '\'' && ch != '}') {
                throw new JSONException(jsonReader.info());
            }
        }

        for (int i = 0; ; i++) {
            if (jsonReader.nextIfMatch('}')) {
                if (object == null) {
                    object = createInstance(jsonReader.getContext().getFeatures() | features);
                }
                break;
            }

            JSONReader.Context context = jsonReader.getContext();
            long features3, hash = jsonReader.readFieldNameHashCode();
            JSONReader.AutoTypeBeforeHandler autoTypeFilter = context.getContextAutoTypeBeforeHandler();
            if (hash == getTypeKeyHash()
                    && ((((features3 = (features | getFeatures() | context.getFeatures())) & JSONReader.Feature.SupportAutoType.mask) != 0) || autoTypeFilter != null)
            ) {
                ObjectReader reader = null;

                long typeHash = jsonReader.readTypeHashCode();
                if (autoTypeFilter != null) {
                    Class<?> filterClass = autoTypeFilter.apply(typeHash, objectClass, features3);
                    if (filterClass == null) {
                        filterClass = autoTypeFilter.apply(jsonReader.getString(), objectClass, features3);
                        if (filterClass != null) {
                            reader = context.getObjectReader(filterClass);
                        }
                    }
                }

                if (reader == null) {
                    reader = autoType(context, typeHash);
                }

                String typeName = null;
                if (reader == null) {
                    typeName = jsonReader.getString();
                    reader = context.getObjectReaderAutoType(
                            typeName, objectClass, features3
                    );

                    if (reader == null) {
                        throw new JSONException(jsonReader.info("No suitable ObjectReader found for" + typeName));
                    }
                }

                if (reader == this) {
                    continue;
                }

                FieldReader fieldReader = reader.getFieldReader(hash);
                if (fieldReader != null && typeName == null) {
                    typeName = jsonReader.getString();
                }

                if (i != 0) {
                    jsonReader.reset(savePoint);
                }

                object = (T) reader.readObject(
                        jsonReader, fieldType, fieldName, features | getFeatures()
                );

                if (fieldReader != null) {
                    fieldReader.accept(object, typeName);
                }

                return object;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | getFeatures())) {
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                fieldReader = getFieldReaderLCase(nameHashCodeLCase);
            }

            if (object == null) {
                object = createInstance(jsonReader.getContext().getFeatures() | features);
            }

            if (fieldReader == null) {
                processExtra(jsonReader, object);
                continue;
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        jsonReader.nextIfMatch(',');

        Function buildFunction = getBuildFunction();
        if (buildFunction != null) {
            object = (T) buildFunction.apply(object);
        }

        if (schema != null) {
            schema.assertValidate(object);
        }

        return object;
    }
}
