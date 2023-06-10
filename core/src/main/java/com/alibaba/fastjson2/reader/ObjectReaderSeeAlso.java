package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONType;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.function.Supplier;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Arrays;

final class ObjectReaderSeeAlso<T>
        extends ObjectReaderAdapter<T> {
    ObjectReaderSeeAlso(
            Class objectType,
            Supplier<T> defaultCreator,
            String typeKey,
            Class[] seeAlso,
            String[] seeAlsoNames,
            Class seeAlsoDefault,
            FieldReader... fieldReaders
    ) {
        super(
                objectType,
                typeKey,
                null,
                JSONReader.Feature.SupportAutoType.mask,
                defaultCreator,
                null,
                seeAlso,
                seeAlsoNames,
                seeAlsoDefault,
                fieldReaders
        );
    }

    ObjectReaderSeeAlso addSubType(Class subTypeClass, String subTypeClassName) {
        for (Class item : seeAlso) {
            if (item == subTypeClass) {
                return this;
            }
        }

        Class[] seeAlso1 = Arrays.copyOf(seeAlso, seeAlso.length + 1);
        String[] seeAlsoNames1 = Arrays.copyOf(this.seeAlsoNames, this.seeAlsoNames.length + 1);
        seeAlso1[seeAlso1.length - 1] = subTypeClass;
        if (subTypeClassName == null) {
            JSONType jsonType = (JSONType) subTypeClass.getAnnotation(JSONType.class);
            if (jsonType != null) {
                subTypeClassName = jsonType.typeName();
            }
        }
        if (subTypeClassName != null) {
            seeAlsoNames1[seeAlsoNames1.length - 1] = subTypeClassName;
        }

        return new ObjectReaderSeeAlso(
                objectClass,
                creator,
                typeKey,
                seeAlso1,
                seeAlsoNames1,
                seeAlsoDefault,
                fieldReaders
        );
    }

    @Override
    public T createInstance(long features) {
        if (creator == null) {
            return null;
        }
        return creator.get();
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.jsonb) {
            return readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (!serializable) {
            jsonReader.errorOnNoneSerializable(objectClass);
        }

        if (jsonReader.nextIfNull()) {
            jsonReader.nextIfComma();
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
        boolean objectStart = jsonReader.nextIfObjectStart();
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
            if (jsonReader.nextIfObjectEnd()) {
                if (object == null) {
                    object = createInstance(jsonReader.context.getFeatures() | features);
                }
                break;
            }

            JSONReader.Context context = jsonReader.context;
            long features3, hash = jsonReader.readFieldNameHashCode();
            JSONReader.AutoTypeBeforeHandler autoTypeFilter = context.getContextAutoTypeBeforeHandler();
            if (hash == getTypeKeyHash()
                    && ((((features3 = (features | getFeatures() | context.getFeatures())) & JSONReader.Feature.SupportAutoType.mask) != 0) || autoTypeFilter != null)
            ) {
                ObjectReader reader = null;
                long typeHash = jsonReader.readTypeHashCode();

                Number typeNumber = null;
                String typeNumberStr = null;
                if (typeHash == -1 && jsonReader.isNumber()) {
                    typeNumber = jsonReader.readNumber();
                    typeNumberStr = typeNumber.toString();
                    typeHash = Fnv.hashCode64(typeNumberStr);
                }
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

                    if (reader == null && seeAlsoDefault != null) {
                        reader = context.getObjectReader(seeAlsoDefault);
                    }

                    if (reader == null) {
                        throw new JSONException(jsonReader.info("No suitable ObjectReader found for" + typeName));
                    }
                }

                if (reader == this) {
                    continue;
                }

                FieldReader fieldReader = reader.getFieldReader(hash);
                if (fieldReader != null && typeName == null) {
                    if (typeNumberStr != null) {
                        typeName = typeNumberStr;
                    } else {
                        typeName = jsonReader.getString();
                    }
                }

                if (i != 0) {
                    jsonReader.reset(savePoint);
                }

                object = (T) reader.readObject(
                        jsonReader, fieldType, fieldName, features | getFeatures()
                );

                if (fieldReader != null) {
                    if (typeNumber != null) {
                        fieldReader.accept(object, typeNumber);
                    } else {
                        fieldReader.accept(object, typeName);
                    }
                }

                return object;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | getFeatures())) {
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                fieldReader = getFieldReaderLCase(nameHashCodeLCase);
            }

            if (object == null) {
                object = createInstance(jsonReader.context.getFeatures() | features);
            }

            if (fieldReader == null) {
                processExtra(jsonReader, object);
                continue;
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        jsonReader.nextIfComma();

        Function buildFunction = getBuildFunction();
        if (buildFunction != null) {
            object = (T) buildFunction.apply(object);
        }

        return object;
    }
}
