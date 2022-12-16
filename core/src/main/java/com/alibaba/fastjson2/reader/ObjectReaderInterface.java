package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ObjectReaderInterface<T>
        extends ObjectReaderAdapter<T> {
    public ObjectReaderInterface(
            Class objectClass,
            String typeKey,
            String typeName,
            long features,
            Supplier creator,
            Function buildFunction,
            FieldReader[] fieldReaders
    ) {
        super(objectClass, typeKey, typeName, features, null, creator, buildFunction, fieldReaders);
    }

    @Override
    public T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        ObjectReader autoTypeReader = jsonReader.checkAutoType(this.objectClass, this.typeNameHash, this.features | features);
        if (autoTypeReader != null && autoTypeReader.getObjectClass() != this.objectClass) {
            return (T) autoTypeReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (jsonReader.isArray()) {
            if (jsonReader.isSupportBeanArray()) {
                return readArrayMappingJSONBObject(jsonReader, fieldType, fieldName, features);
            } else {
                throw new JSONException(jsonReader.info("expect object, but " + JSONB.typeName(jsonReader.getType())));
            }
        }

        boolean objectStart = jsonReader.nextIfObjectStart();

        JSONObject jsonObject = new JSONObject();
        Object object = null;
        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long hash = jsonReader.readFieldNameHashCode();
            if (hash == typeKeyHashCode && i == 0) {
                long typeHash = jsonReader.readValueHashCode();
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

                jsonReader.setTypeRedirect(true);
                return (T) autoTypeObjectReader.readJSONBObject(jsonReader, fieldType, fieldName, features);
            }

            if (hash == 0) {
                continue;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | this.features)) {
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();
                if (nameHashCodeLCase != hash) {
                    fieldReader = getFieldReaderLCase(nameHashCodeLCase);
                }
            }
            if (fieldReader == null) {
                jsonObject.put(jsonReader.getFieldName(), jsonReader.readAny());
            } else {
                Object fieldValue = fieldReader.readFieldValue(jsonReader);
                jsonObject.put(fieldReader.fieldName, fieldValue);
            }
        }

        // GraalVM not support
        object = (T) Proxy.newProxyInstance(objectClass.getClassLoader(), new Class[]{objectClass}, jsonObject);
        if (schema != null) {
            schema.assertValidate(object);
        }

        return (T) object;
    }

    @Override
    public T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        if (jsonReader.nextIfNull()) {
            jsonReader.nextIfMatch(',');
            return null;
        }

        if (jsonReader.isArray() && jsonReader.isSupportBeanArray(getFeatures() | features)) {
            return readArrayMappingObject(jsonReader, fieldType, fieldName, features);
        }

        T object = null;
        JSONObject jsonObject = new JSONObject();
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

            if (fieldReader == null) {
                jsonObject.put(jsonReader.getFieldName(), jsonReader.readAny());
            } else {
                Object fieldValue = fieldReader.readFieldValue(jsonReader);
                jsonObject.put(fieldReader.fieldName, fieldValue);
            }
        }

        jsonReader.nextIfMatch(',');

        // GraalVM not support
        object = (T) Proxy.newProxyInstance(objectClass.getClassLoader(), new Class[]{objectClass}, jsonObject);
        Function buildFunction = getBuildFunction();
        if (buildFunction != null) {
            object = (T) buildFunction.apply(object);
        }

        if (schema != null) {
            schema.assertValidate(object);
        }

        return object;
    }

    @Override
    public T createInstance(long features) {
        JSONObject object = new JSONObject();
        // GraalVM not support
        return (T) Proxy.newProxyInstance(objectClass.getClassLoader(), new Class[]{objectClass}, object);
    }

    @Override
    public T createInstance(Map map, long features) {
        JSONObject object;
        if (map instanceof JSONObject) {
            object = (JSONObject) map;
        } else {
            object = new JSONObject(map);
        }
        // GraalVM not support
        return (T) Proxy.newProxyInstance(objectClass.getClassLoader(), new Class[]{objectClass}, object);
    }
}
