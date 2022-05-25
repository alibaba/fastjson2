package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.util.function.Function;

import static com.alibaba.fastjson2.JSONB.Constants.BC_TYPED_ANY;

public abstract class ObjectReaderBean<T>
        implements ObjectReader<T> {
    protected final Class objectClass;
    protected final String typeName;
    protected final long typeNameHash;

    protected FieldReader extraFieldReader;

    protected boolean hasDefaultValue;

    protected final JSONSchema schema;

    protected ObjectReaderBean(Class objectClass, String typeName, JSONSchema schema) {
        if (typeName == null) {
            if (objectClass != null) {
                typeName = TypeUtils.getTypeName(objectClass);
            }
        }

        this.objectClass = objectClass;
        this.typeName = typeName;
        this.typeNameHash = typeName != null ? Fnv.hashCode64(typeName) : 0;

        this.schema = schema;
    }

    @Override
    public Class<T> getObjectClass() {
        return objectClass;
    }

    protected void processExtra(JSONReader jsonReader, Object object) {
        if (extraFieldReader == null || object == null) {
            jsonReader.skipValue();
            return;
        }

        extraFieldReader.processExtra(jsonReader, object);
    }

    public ObjectReader checkAutoType(JSONReader jsonReader, Class listClass, long features) {
        ObjectReader autoTypeObjectReader = null;
        if (jsonReader.nextIfMatch(BC_TYPED_ANY)) {
            long typeHash = jsonReader.readTypeHashCode();
            JSONReader.Context context = jsonReader.getContext();
            autoTypeObjectReader = context.getObjectReaderAutoType(typeHash);

            if (autoTypeObjectReader == null) {
                String typeName = jsonReader.getString();
                autoTypeObjectReader = context.getObjectReaderAutoType(typeName, listClass, features);
            }

            if (autoTypeObjectReader == null) {
                throw new JSONException("auotype not support : " + jsonReader.getString());
            }

            if (typeHash == this.typeNameHash) {
                return this;
            }

            boolean isSupportAutoType = ((context.getFeatures() | features) & JSONReader.Feature.SupportAutoType.mask) != 0;
            if (!isSupportAutoType) {
                return null;
//                throw new JSONException("autoType not support input " + jsonReader.getString());
            }
        }
        return autoTypeObjectReader;
    }

    protected void initDefaultValue(T object) {
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

        if (jsonReader.isArray() && jsonReader.isSupportBeanArray(getFeatures() | features)) {
            return readArrayMappingObject(jsonReader);
        }

        T object = null;
        jsonReader.nextIfMatch('{');

        for (int i = 0; ; i++) {
            if (jsonReader.nextIfMatch('}')) {
                if (object == null) {
                    object = createInstance(jsonReader.getContext().getFeatures() | features);
                }
                break;
            }

            JSONReader.Context context = jsonReader.getContext();
            long features3, hash = jsonReader.readFieldNameHashCode();
            if (i == 0
                    && hash == getTypeKeyHash()
                    && ((((features3 = (features | getFeatures() | context.getFeatures())) & JSONReader.Feature.SupportAutoType.mask) != 0) || context.getContextAutoTypeBeforeHandler() != null)
            ) {
                long typeHash = jsonReader.readTypeHashCode();

                ObjectReader reader = autoType(context, typeHash);

                String typeName = null;
                if (reader == null) {
                    typeName = jsonReader.getString();
                    reader = context.getObjectReaderAutoType(
                            typeName, getObjectClass(), features3
                    );

                    if (reader == null) {
                        throw new JSONException("No suitable ObjectReader found for" + typeName);
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
                        jsonReader, features | getFeatures()
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
                if (this instanceof ObjectReaderBean) {
                    processExtra(jsonReader, object);
                } else {
                    jsonReader.skipValue();
                }
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
