package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.filter.ExtraProcessor;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;

public abstract class ObjectReaderBean<T>
        implements ObjectReader<T> {
    protected final Class objectClass;
    protected final Supplier<T> creator;
    protected final Function buildFunction;
    protected final long features;
    protected final String typeName;
    protected final long typeNameHash;

    protected FieldReader extraFieldReader;

    protected boolean hasDefaultValue;
    protected boolean serializable;

    protected final JSONSchema schema;

    protected ObjectReaderBean(Class objectClass, Supplier<T> creator, String typeName, long features, JSONSchema schema, Function buildFunction) {
        if (typeName == null) {
            if (objectClass != null) {
                typeName = TypeUtils.getTypeName(objectClass);
            }
        }

        this.objectClass = objectClass;
        this.creator = creator;
        this.buildFunction = buildFunction;
        this.features = features;
        this.typeName = typeName;
        this.typeNameHash = typeName != null ? Fnv.hashCode64(typeName) : 0;

        this.schema = schema;
        this.serializable = objectClass != null && Serializable.class.isAssignableFrom(objectClass);
    }

    @Override
    public Class<T> getObjectClass() {
        return objectClass;
    }

    protected T processObjectInputSingleItemArray(JSONReader jsonReader,
                                                  Type fieldType,
                                                  Object fieldName,
                                                  long features) {
        String message = "expect {, but [, class " + this.typeName;
        if (fieldName != null) {
            message += ", parent fieldName " + fieldName;
        }
        String info = jsonReader.info(message);

        long featuresAll = jsonReader.features(features);
        if ((featuresAll & JSONReader.Feature.SupportSmartMatch.mask) != 0) {
            Type itemType = fieldType == null ? this.objectClass : fieldType;
            List list = jsonReader.readArray(itemType);
            if (list.size() == 1) {
                return (T) list.get(0);
            }
        }
        throw new JSONException(info);
    }

    protected void processExtra(JSONReader jsonReader, Object object) {
        if (extraFieldReader != null && object != null) {
            extraFieldReader.processExtra(jsonReader, object);
            return;
        }

        if ((jsonReader.features(features) & JSONReader.Feature.SupportSmartMatch.mask) != 0) {
            String fieldName = jsonReader.getFieldName();
            if (fieldName.startsWith("is")) {
                String fieldName1 = fieldName.substring(2);
                long hashCode64LCase = Fnv.hashCode64LCase(fieldName1);
                FieldReader fieldReader = getFieldReaderLCase(hashCode64LCase);
                if (fieldReader != null && fieldReader.fieldClass == Boolean.class) {
                    fieldReader.readFieldValue(jsonReader, object);
                    return;
                }
            }
        }

        ExtraProcessor extraProcessor = jsonReader.getContext().getExtraProcessor();
        if (extraProcessor != null) {
            String fieldName = jsonReader.getFieldName();
            Type type = extraProcessor.getType(fieldName);
            Object extraValue = jsonReader.read(type);
            extraProcessor.processExtra(object, fieldName, extraValue);
            return;
        }

        jsonReader.skipValue();
    }

    public void acceptExtra(Object object, String fieldName, Object fieldValue) {
        if (extraFieldReader == null || object == null) {
            return;
        }
        extraFieldReader.acceptExtra(object, fieldName, fieldValue);
    }

    public ObjectReader checkAutoType(JSONReader jsonReader, Class expectClass, long features) {
        if (jsonReader.nextIfMatch(BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            JSONReader.Context context = jsonReader.getContext();
            JSONReader.AutoTypeBeforeHandler autoTypeFilter = context.getContextAutoTypeBeforeHandler();
            if (autoTypeFilter != null) {
                Class<?> filterClass = autoTypeFilter.apply(typeHash, expectClass, features);
                if (filterClass == null) {
                    String typeName = jsonReader.getString();
                    filterClass = autoTypeFilter.apply(typeName, expectClass, features);
                }

                if (filterClass != null) {
                    return context.getObjectReader(filterClass);
                }
            }

            ObjectReader autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);
            if (autoTypeObjectReader == null) {
                String typeName = jsonReader.getString();
                autoTypeObjectReader = context.getObjectReaderAutoType(typeName, expectClass, features);
            }

            if (autoTypeObjectReader == null) {
                throw new JSONException(jsonReader.info("auotype not support"));
            }

            if (typeHash == this.typeNameHash) {
                return this;
            }

            boolean isSupportAutoType = ((context.getFeatures() | features) & JSONReader.Feature.SupportAutoType.mask) != 0;
            if (!isSupportAutoType) {
                return null;
//                throw new JSONException("autoType not support input " + jsonReader.getString());
            }

            return autoTypeObjectReader;
        }
        return null;
    }

    protected void initDefaultValue(T object) {
    }

    public void readObject(JSONReader jsonReader, Object object, long features) {
        if (jsonReader.nextIfNull()) {
            jsonReader.nextIfMatch(',');
            return;
        }

        boolean objectStart = jsonReader.nextIfMatch('{');
        if (!objectStart) {
            throw new JSONException(jsonReader.info());
        }

        while (true) {
            if (jsonReader.nextIfMatch('}')) {
                break;
            }

            long hash = jsonReader.readFieldNameHashCode();
            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | getFeatures())) {
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                fieldReader = getFieldReaderLCase(nameHashCodeLCase);
            }

            if (fieldReader == null) {
                processExtra(jsonReader, object);
                continue;
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        jsonReader.nextIfMatch(',');

        if (schema != null) {
            schema.assertValidate(object);
        }
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (jsonReader.nextIfNullOrEmptyString()) {
            jsonReader.nextIfMatch(',');
            return null;
        }

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
            if (i == 0
                    && hash == getTypeKeyHash()
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

                object = (T) reader.readObject(
                        jsonReader, null, null, features | getFeatures()
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
