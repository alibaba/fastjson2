package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked"})
public interface ObjectReader<T> {

    long HASH_TYPE = Fnv.hashCode64("@type");

    /**
     * @return {@link T}
     * @throws UnsupportedOperationException If the method is not overloaded or otherwise
     */
    default T createInstance() {
        return createInstance(0);
    }

    /**
     * @return {@link T}
     * @throws UnsupportedOperationException If the method is not overloaded or otherwise
     */
    default T createInstance(long features) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return {@link T}
     * @throws UnsupportedOperationException If the method is not overloaded or otherwise
     */
    default T createInstance(Collection collection) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return {@link T}
     * @throws JSONException If a suitable ObjectReader is not found
     */
    default T createInstance(Map map) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Object typeKey = map.get(getTypeKey());

        if (typeKey instanceof String) {
            String typeName = (String) typeKey;
            long typeHash = Fnv.hashCode64(typeName);
            ObjectReader<T> reader = autoType(provider, typeHash);

            if (reader == null) {
                reader = provider.getObjectReader(
                        typeName, getObjectClass(), getFeatures()
                );

                if (reader == null) {
                    throw new JSONException("No suitable ObjectReader found for" + typeName);
                }
            }

            if (reader != this) {
                return reader.createInstance(map);
            }
        }

        T object = createInstance(0L);
        for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
            FieldReader fieldReader = getFieldReader(
                    entry.getKey().toString()
            );
            if (fieldReader == null) {
                continue;
            }

            Object fieldValue = entry.getValue();
            Class fieldClass = fieldReader.getFieldClass();

            if (fieldValue != null) {
                Class<?> valueClass = fieldValue.getClass();

                if (valueClass != fieldClass) {
                    Function typeConvert = provider.getTypeConvert(valueClass, fieldClass);

                    if (typeConvert != null) {
                        fieldValue = typeConvert.apply(fieldValue);
                    }
                }
            }

            if (!fieldClass.isInstance(fieldValue)) {
                if (fieldReader.getFormat() == null) {
                    fieldValue = TypeUtils.cast(fieldValue, fieldClass);
                }
            }

            fieldReader.accept(object, fieldValue);
        }

        Function buildFunction = getBuildFunction();
        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return object;
    }

    /**
     * @throws UnsupportedOperationException If the method is not overloaded or otherwise
     */
    default T createInstanceNoneDefaultConstructor(Map<Long, Object> values) {
        throw new UnsupportedOperationException();
    }

    /**
     * Features enabled by ObjectReader
     */
    default long getFeatures() {
        return 0L;
    }

    default String getTypeKey() {
        return "@type";
    }

    default long getTypeKeyHash() {
        return HASH_TYPE;
    }

    default Class<T> getObjectClass() {
        return null;
    }

    default FieldReader getFieldReader(long hashCode) {
        return null;
    }

    default FieldReader getFieldReaderLCase(long hashCode) {
        return null;
    }

    default boolean setFieldValue(Object object, String fieldName, long fieldNameHashCode, int value) {
        FieldReader fieldReader = getFieldReader(fieldNameHashCode);
        if (fieldReader == null) {
            return false;
        }
        fieldReader.accept(object, value);
        return true;
    }

    default boolean setFieldValue(Object object, String fieldName, long fieldNameHashCode, long value) {
        FieldReader fieldReader = getFieldReader(fieldNameHashCode);
        if (fieldReader == null) {
            return false;
        }
        fieldReader.accept(object, value);
        return true;
    }

    default FieldReader getFieldReader(String fieldName) {
        FieldReader fieldReader = getFieldReader(
                Fnv.hashCode64(fieldName)
        );

        if (fieldReader == null) {
            String fieldNameLCase = fieldName.toLowerCase();

            if (!fieldNameLCase.equals(fieldName)) {
                fieldReader = getFieldReader(
                        Fnv.hashCode64(fieldNameLCase)
                );
            }
        }

        return fieldReader;
    }

    default Function getBuildFunction() {
        return null;
    }

    default ObjectReader autoType(JSONReader.Context context, long typeHash) {
        return context.getObjectReaderAutoType(typeHash);
    }

    default ObjectReader autoType(ObjectReaderProvider provider, long typeHash) {
        return provider.getObjectReader(typeHash);
    }

    /**
     * @return {@link T}
     * @throws JSONException If a suitable ObjectReader is not found
     */
    default T readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.isArray() &&
                jsonReader.isSupportBeanArray()) {
            return readArrayMappingJSONBObject(jsonReader);
        }

        T object = null;
        jsonReader.nextIfObjectStart();

        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }
            long hash = jsonReader.readFieldNameHashCode();

            if (hash == getTypeKeyHash() && i == 0) {
                long typeHash = jsonReader.readTypeHashCode();
                JSONReader.Context context = jsonReader.getContext();
                ObjectReader reader = autoType(context, typeHash);

                if (reader == null) {
                    String typeName = jsonReader.getString();
                    reader = context.getObjectReaderAutoType(typeName, null);

                    if (reader == null) {
                        throw new JSONException("No suitable ObjectReader found for" + typeName);
                    }
                }

                if (reader == this) {
                    continue;
                }

                return (T) reader.readJSONBObject(jsonReader, features);
            }

            if (hash == 0) {
                continue;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | getFeatures())) {
                long nameHashCodeLCase = jsonReader.getNameHashCodeLCase();

                if (nameHashCodeLCase != hash) {
                    fieldReader = getFieldReaderLCase(nameHashCodeLCase);
                }
            }

            if (fieldReader == null) {
                jsonReader.skipValue();
                continue;
            }

            if (object == null) {
                object = createInstance(jsonReader.getContext().getFeatures() | features);
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        if (object == null) {
            object = createInstance(jsonReader.getContext().getFeatures() | features);
        }

        return object;
    }

    /**
     * @return {@link T}
     * @throws UnsupportedOperationException If the method is not overloaded or otherwise
     */
    default T readArrayMappingJSONBObject(JSONReader jsonReader) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return {@link T}
     * @throws UnsupportedOperationException If the method is not overloaded or otherwise
     */
    default T readArrayMappingObject(JSONReader jsonReader) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return {@link T}
     */
    default T readObject(JSONReader jsonReader) {
        return readObject(jsonReader, getFeatures());
    }

    /**
     * @return {@link T}
     * @throws JSONException If a suitable ObjectReader is not found
     */
    default T readObject(JSONReader jsonReader, long features) {
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

            long features3, hash = jsonReader.readFieldNameHashCode();
            if (hash == getTypeKeyHash() && i == 0 &&
                    ((features3 = (features | getFeatures() | jsonReader.getContext().getFeatures())) & JSONReader.Feature.SupportAutoType.mask) != 0
            ) {
                long typeHash = jsonReader.readTypeHashCode();
                JSONReader.Context context = jsonReader.getContext();
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

            if (fieldReader == null) {
                jsonReader.skipValue();
                continue;
            }

            if (object == null) {
                object = createInstance(jsonReader.getContext().getFeatures() | features);
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        jsonReader.nextIfMatch(',');

        Function buildFunction = getBuildFunction();
        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return object;
    }
}
