package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
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
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default T createInstance(Map map, JSONReader.Feature... features) {
        long featuresValue = 0;
        for (JSONReader.Feature feature : features) {
            featuresValue |= feature.mask;
        }
        return createInstance(map, featuresValue);
    }
    /**
     * @return {@link T}
     * @throws JSONException If a suitable ObjectReader is not found
     */
    default T createInstance(Map map, long features) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Object typeKey = map.get(getTypeKey());

        if (typeKey instanceof String) {
            String typeName = (String) typeKey;
            long typeHash = Fnv.hashCode64(typeName);
            ObjectReader<T> reader = null;
            if ((features & JSONReader.Feature.SupportAutoType.mask) != 0 || this instanceof ObjectReaderSeeAlso) {
                reader = autoType(provider, typeHash);
            }

            if (reader == null) {
                reader = provider.getObjectReader(
                        typeName, getObjectClass(), features | getFeatures()
                );

                if (reader == null) {
                    throw new JSONException("No suitable ObjectReader found for" + typeName);
                }
            }

            if (reader != this) {
                return reader.createInstance(map, features);
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
            Type fieldType = fieldReader.getFieldType();

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

            Object typedFieldValue;
            if (fieldValue == null || fieldType == fieldValue.getClass()) {
                typedFieldValue = fieldValue;
            } else {
                if (fieldValue instanceof JSONObject) {
                    typedFieldValue = ((JSONObject) fieldValue).toJavaObject(fieldType);
                } else if (fieldValue instanceof JSONArray) {
                    typedFieldValue = ((JSONArray) fieldValue).toJavaObject(fieldType);
                } else {
                    String fieldValueJSONString = JSON.toJSONString(fieldValue);
                    try(JSONReader jsonReader = JSONReader.of(fieldValueJSONString)) {
                        ObjectReader fieldObjectReader = fieldReader.getObjectReader(jsonReader);
                        typedFieldValue = fieldObjectReader.readObject(jsonReader, 0);
                    }
                }
            }
            fieldReader.accept(object, typedFieldValue);
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
    T readObject(JSONReader jsonReader, long features);
}
