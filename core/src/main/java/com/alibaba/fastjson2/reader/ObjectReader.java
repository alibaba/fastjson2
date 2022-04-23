package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public interface ObjectReader<T> {
    long HASH_TYPE = Fnv.hashCode64("@type");

    default Class getObjectClass() {
        return null;
    }

    default long getFeatures() {
        return 0;
    }

    default T createInstance() {
        throw new UnsupportedOperationException();
    }

    default T createInstance(Collection collection) {
        throw new UnsupportedOperationException();
    }

    default T createInstance(Map map) {
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();

        Object typeKeyValue = map.get(getTypeKey());
        if (typeKeyValue instanceof String) {
            String typeName = (String) typeKeyValue;
            long typeHash = Fnv.hashCode64(typeName);
            ObjectReader autoTypeObjectReader = autoType(provider, typeHash);

            if (autoTypeObjectReader == null) {
                autoTypeObjectReader = provider.getObjectReader(typeName, getObjectClass(), getFeatures());

                if (autoTypeObjectReader == null) {
                    throw new JSONException("auotype not support : " + typeName);
                }
            }

            if (autoTypeObjectReader != this) {
                return (T) autoTypeObjectReader.createInstance(map);
            }
        }

        Object object = createInstance();
        for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = it.next();
            String fieldName = entry.getKey().toString();
            Object fieldValue = entry.getValue();

            FieldReader fieldReader = getFieldReader(fieldName);
            if (fieldReader != null) {
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
        }

        Function buildFunction = getBuildFunction();
        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }
        return (T) object;
    }

    default String getTypeKey() {
        return "@type";
    }

    default long getTypeKeyHash() {
        return HASH_TYPE;
    }

    default T createInstanceNoneDefaultConstructor(Map<Long, Object> values) {
        throw new UnsupportedOperationException();
    }

    default FieldReader getFieldReader(long hashCode) {
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

    default FieldReader getFieldReaderLCase(long hashCode) {
        return null;
    }

    default FieldReader getFieldReader(String fieldName) {
        FieldReader fieldReader = getFieldReader(Fnv.hashCode64(fieldName));
        if (fieldReader == null) {
            String fieldNameLCase = fieldName.toLowerCase();
            if (!fieldNameLCase.equals(fieldName)) {
                fieldReader = getFieldReader(Fnv.hashCode64(fieldNameLCase));
            }
        }
        return fieldReader;
    }

    default T readJSONBObject(JSONReader jsonReader, long features) {
        if (jsonReader.isArray()
                && jsonReader.isSupportBeanArray()) {
            return readArrayMappingJSONBObject(jsonReader);
        }

        jsonReader.nextIfObjectStart();

        Object object = null;
        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }

            long hash = jsonReader.readFieldNameHashCode();
            if (hash == getTypeKeyHash() && i == 0) {
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

            if (hash == 0) {
                continue;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features | this.getFeatures())) {
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
                object = createInstance();
            }
            fieldReader.readFieldValue(jsonReader, object);
        }

        if (object == null) {
            object = createInstance();
        }
        return (T) object;
    }

    default T readArrayMappingJSONBObject(JSONReader jsonReader) {
        throw new UnsupportedOperationException();
    }

    default T readArrayMappingObject(JSONReader jsonReader) {
        throw new UnsupportedOperationException();
    }

    default ObjectReader autoType(JSONReader.Context context, long typeHash) {
        return context.getObjectReaderAutoType(typeHash);
    }

    default ObjectReader autoType(ObjectReaderProvider provider, long typeHash) {
        return provider.getObjectReader(typeHash);
    }

    default Function getBuildFunction() {
        return null;
    }

    default T readObject(JSONReader jsonReader) {
        return readObject(jsonReader, getFeatures());
    }

    default T readObject(JSONReader jsonReader, long features) {
        if (jsonReader.isJSONB()) {
            return readJSONBObject(jsonReader, features);
        }

        if (jsonReader.isArray()
                && jsonReader.isSupportBeanArray(getFeatures() | features)) {
            return readArrayMappingObject(jsonReader);
        }

        jsonReader.nextIfMatch('{');


        Object object = null;
        for (int i = 0; ; i++) {
            if (jsonReader.nextIfMatch('}')) {
                if (object == null) {
                    object = createInstance();
                }
                break;
            }

            long hash = jsonReader.readFieldNameHashCode();
            long features3;
            if (hash == getTypeKeyHash()
                    && i == 0
                    && ((features3 = (features | getFeatures() | jsonReader.getContext().getFeatures())) & JSONReader.Feature.SupportAutoType.mask) != 0
            ) {
                long typeHash = jsonReader.readTypeHashCode();
                String typeName = null;
                JSONReader.Context context = jsonReader.getContext();
                ObjectReader autoTypeObjectReader = autoType(context, typeHash);
                if (autoTypeObjectReader == null) {
                    typeName = jsonReader.getString();
                    autoTypeObjectReader = context.getObjectReaderAutoType(typeName, getObjectClass(), features3);

                    if (autoTypeObjectReader == null) {
                        throw new JSONException("auotype not support : " + typeName);
                    }
                }

                if (autoTypeObjectReader == this) {
                    continue;
                }

                FieldReader typeFieldReader = autoTypeObjectReader.getFieldReader(hash);
                if (typeFieldReader != null && typeName == null) {
                    typeName = jsonReader.getString();
                }

                object = autoTypeObjectReader.readObject(jsonReader, features | getFeatures());
                if (typeFieldReader != null) {
                    typeFieldReader.accept(object, typeName);
                }
                return (T) object;
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
                object = createInstance();
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        jsonReader.nextIfMatch(',');

        Function buildFunction = getBuildFunction();
        if (buildFunction != null) {
            return (T) buildFunction.apply(object);
        }

        return (T) object;
    }

    /**
     * Specify {@link Type} to get its {@link ObjectReader} instance, not singleton mode
     *
     * @since 2.0.2
     */
    static <T> ObjectReader<T> getInstance(Type type) {
        return JSONFactory.getDefaultObjectReaderProvider().getObjectReader(type, false);
    }

    /**
     * Specify {@link Type} to get its {@link ObjectReader} instance, not singleton mode
     *
     * @since 2.0.2
     */
    static <T> ObjectReader<T> getInstance(Type type, boolean fieldBased) {
        return JSONFactory.getDefaultObjectReaderProvider().getObjectReader(type, fieldBased);
    }
}
