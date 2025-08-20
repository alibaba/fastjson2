package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * ObjectReader is responsible for deserializing JSON data into Java objects.
 * It provides a set of methods for creating object instances, reading JSON data,
 * and handling field mapping.
 *
 * <p>This interface supports various features including:
 * <ul>
 *   <li>Object creation with different parameter types</li>
 *   <li>JSON object and array reading</li>
 *   <li>Field mapping and value setting</li>
 *   <li>Auto-type support for polymorphic deserialization</li>
 *   <li>Custom feature configuration</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>
 * // Get an ObjectReader for a specific type
 * ObjectReader&lt;User&gt; reader = JSONFactory.getDefaultObjectReaderProvider().getObjectReader(User.class);
 *
 * // Read from JSON string
 * String jsonString = "{\"id\":1,\"name\":\"John\"}";
 * User user = reader.readObject(JSONReader.of(jsonString));
 *
 * // Create instance with map data
 * Map&lt;String, Object&gt; data = new HashMap&lt;&gt;();
 * data.put("id", 1);
 * data.put("name", "John");
 * User user2 = reader.createInstance(data);
 * </pre>
 *
 * @param <T> the type of objects that this ObjectReader can deserialize
 * @since 2.0.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public interface ObjectReader<T> {
    long HASH_TYPE = Fnv.hashCode64("@type");
    String VALUE_NAME = "@value";

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
        return createInstance(collection, 0L);
    }

    /**
     * @return {@link T}
     * @throws UnsupportedOperationException If the method is not overloaded or otherwise
     */
    default T createInstance(Collection collection, JSONReader.Feature... features) {
        return createInstance(collection, JSONReader.Feature.of(features));
    }

    /**
     * @return {@link T}
     * @throws UnsupportedOperationException If the method is not overloaded or otherwise
     */
    default T createInstance(Collection collection, long features) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    /**
     * Accepts extra field data that is not mapped to any specific field in the object.
     * This method is called when deserializing JSON objects that contain fields not
     * present in the target class.
     *
     * @param object the object being deserialized
     * @param fieldName the name of the extra field
     * @param fieldValue the value of the extra field
     */
    default void acceptExtra(Object object, String fieldName, Object fieldValue) {
        acceptExtra(object, fieldName, fieldValue, this.getFeatures());
    }

    /**
     * Accepts extra field data that is not mapped to any specific field in the object.
     * This method is called when deserializing JSON objects that contain fields not
     * present in the target class.
     *
     * @param object the object being deserialized
     * @param fieldName the name of the extra field
     * @param fieldValue the value of the extra field
     * @param features the JSON reader features to use
     */
    default void acceptExtra(Object object, String fieldName, Object fieldValue, long features) {
    }

    /**
     * Creates an instance of the object type from a map of field values using the specified features.
     *
     * @param map the map containing field names and values
     * @param features the JSON reader features to use
     * @return a new instance of the object populated with values from the map
     */
    default T createInstance(Map map, JSONReader.Feature... features) {
        long featuresValue = 0;
        for (int i = 0; i < features.length; i++) {
            featuresValue |= features[i].mask;
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
            }

            if (reader != this && reader != null) {
                return reader.createInstance(map, features);
            }
        }

        T object = createInstance(0L);
        return accept(object, map, features);
    }

    /**
     * Accepts field values from a map and populates the specified object instance.
     *
     * @param object the object instance to populate
     * @param map the map containing field names and values
     * @param features the JSON reader features to use
     * @return the populated object instance
     */
    default T accept(T object, Map map, long features) {
        for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
            String entryKey = entry.getKey().toString();
            Object fieldValue = entry.getValue();

            FieldReader fieldReader = getFieldReader(entryKey);
            if (fieldReader == null) {
                acceptExtra(object, entryKey, entry.getValue(), features);
                continue;
            }

            fieldReader.acceptAny(object, fieldValue, features);
        }

        Function buildFunction = getBuildFunction();
        return buildFunction != null
                ? (T) buildFunction.apply(object)
                : object;
    }

    /**
     * Creates an instance of the object type using a non-default constructor with the specified values.
     *
     * @param values the map of field hash codes to values
     * @return a new instance of the object created with a non-default constructor
     * @throws UnsupportedOperationException if the method is not overloaded or otherwise
     */
    default T createInstanceNoneDefaultConstructor(Map<Long, Object> values) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the features enabled by this ObjectReader.
     *
     * @return the enabled features as a bit mask
     */
    default long getFeatures() {
        return 0L;
    }

    /**
     * Gets the type key used for auto-type support. This key is used to identify
     * the type information in JSON objects.
     *
     * @return the type key string
     */
    default String getTypeKey() {
        return "@type";
    }

    /**
     * Gets the hash code of the type key used for auto-type support.
     *
     * @return the hash code of the type key
     */
    default long getTypeKeyHash() {
        return HASH_TYPE;
    }

    /**
     * Gets the class of objects that this ObjectReader can deserialize.
     *
     * @return the object class, or null if not specified
     */
    default Class<T> getObjectClass() {
        return null;
    }

    /**
     * Gets the FieldReader for the specified field hash code.
     *
     * @param hashCode the hash code of the field name
     * @return the FieldReader for the field, or null if not found
     */
    default FieldReader getFieldReader(long hashCode) {
        return null;
    }

    /**
     * Gets the FieldReader for the specified field hash code, using lowercase matching.
     *
     * @param hashCode the hash code of the field name (lowercase)
     * @return the FieldReader for the field, or null if not found
     */
    default FieldReader getFieldReaderLCase(long hashCode) {
        return null;
    }

    /**
     * Sets the value of a field in the specified object.
     *
     * @param object the object in which to set the field value
     * @param fieldName the name of the field to set
     * @param fieldNameHashCode the hash code of the field name
     * @param value the integer value to set
     * @return true if the field was successfully set, false otherwise
     */
    default boolean setFieldValue(Object object, String fieldName, long fieldNameHashCode, int value) {
        FieldReader fieldReader = getFieldReader(fieldNameHashCode);
        if (fieldReader == null) {
            return false;
        }
        fieldReader.accept(object, value);
        return true;
    }

    /**
     * Sets the value of a field in the specified object.
     *
     * @param object the object in which to set the field value
     * @param fieldName the name of the field to set
     * @param fieldNameHashCode the hash code of the field name
     * @param value the long value to set
     * @return true if the field was successfully set, false otherwise
     */
    default boolean setFieldValue(Object object, String fieldName, long fieldNameHashCode, long value) {
        FieldReader fieldReader = getFieldReader(fieldNameHashCode);
        if (fieldReader == null) {
            return false;
        }
        fieldReader.accept(object, value);
        return true;
    }

    /**
     * Gets the FieldReader for the specified field name.
     *
     * @param fieldName the name of the field
     * @return the FieldReader for the field, or null if not found
     */
    default FieldReader getFieldReader(String fieldName) {
        long fieldNameHash = Fnv.hashCode64(fieldName);
        FieldReader fieldReader = getFieldReader(fieldNameHash);

        if (fieldReader == null) {
            fieldReader = getFieldReaderLCase(fieldNameHash);
            if (fieldReader == null) {
                long fieldNameHashLCase = Fnv.hashCode64LCase(fieldName);
                if (fieldNameHashLCase != fieldNameHash) {
                    fieldReader = getFieldReaderLCase(fieldNameHashLCase);
                }
            }
        }

        return fieldReader;
    }

    /**
     * Sets the value of a field in the specified object.
     *
     * @param object the object in which to set the field value
     * @param fieldName the name of the field to set
     * @param value the object value to set
     * @return true if the field was successfully set, false otherwise
     */
    default boolean setFieldValue(Object object, String fieldName, Object value) {
        FieldReader fieldReader = getFieldReader(fieldName);
        if (fieldReader == null) {
            return false;
        }
        fieldReader.accept(object, value);
        return true;
    }

    /**
     * Gets the build function used to construct the final object instance.
     *
     * @return the build function, or null if not specified
     */
    default Function getBuildFunction() {
        return null;
    }

    /**
     * Resolves an ObjectReader for the specified type hash using the JSON reader context.
     *
     * @param context the JSON reader context
     * @param typeHash the hash code of the type name
     * @return the ObjectReader for the type, or null if not found
     */
    default ObjectReader autoType(JSONReader.Context context, long typeHash) {
        return context.getObjectReaderAutoType(typeHash);
    }

    /**
     * Resolves an ObjectReader for the specified type hash using the ObjectReaderProvider.
     *
     * @param provider the ObjectReaderProvider
     * @param typeHash the hash code of the type name
     * @return the ObjectReader for the type, or null if not found
     */
    default ObjectReader autoType(ObjectReaderProvider provider, long typeHash) {
        return provider.getObjectReader(typeHash);
    }

    /**
     * Reads an object from JSONB format.
     *
     * @param jsonReader the JSONReader to use for parsing
     * @param fieldType the type of the field being read
     * @param fieldName the name of the field being read
     * @param features the features to use
     * @return the deserialized object
     * @throws JSONException if a suitable ObjectReader is not found
     */
    default T readJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        if (jsonReader.isArray() &&
                jsonReader.isSupportBeanArray()) {
            return readArrayMappingJSONBObject(jsonReader, fieldType, fieldName, features);
        }

        T object = null;
        jsonReader.nextIfObjectStart();

        JSONReader.Context context = jsonReader.getContext();
        long features2 = context.getFeatures() | features;
        for (int i = 0; ; ++i) {
            if (jsonReader.nextIfObjectEnd()) {
                break;
            }
            long hash = jsonReader.readFieldNameHashCode();

            if (hash == getTypeKeyHash() && i == 0) {
                long typeHash = jsonReader.readTypeHashCode();
                ObjectReader reader = autoType(context, typeHash);

                if (reader == null) {
                    String typeName = jsonReader.getString();
                    reader = context.getObjectReaderAutoType(typeName, null);

                    if (reader == null) {
                        throw new JSONException(jsonReader.info("No suitable ObjectReader found for " + typeName));
                    }
                }

                if (reader == this) {
                    continue;
                }

                return (T) reader.readJSONBObject(jsonReader, fieldType, fieldName, features);
            }

            if (hash == 0) {
                continue;
            }

            FieldReader fieldReader = getFieldReader(hash);
            if (fieldReader == null && jsonReader.isSupportSmartMatch(features2 | getFeatures())) {
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
                object = createInstance(features2);
            }

            fieldReader.readFieldValue(jsonReader, object);
        }

        return object != null
                ? object
                : createInstance(features2);
    }

    /**
     * Reads an object from JSONB format with array mapping.
     *
     * @param jsonReader the JSONReader to use for parsing
     * @param fieldType the type of the field being read
     * @param fieldName the name of the field being read
     * @param features the features to use
     * @return the deserialized object
     * @throws UnsupportedOperationException if the method is not overloaded or otherwise
     */
    default T readArrayMappingJSONBObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads an object from JSON format with array mapping.
     *
     * @param jsonReader the JSONReader to use for parsing
     * @param fieldType the type of the field being read
     * @param fieldName the name of the field being read
     * @param features the features to use
     * @return the deserialized object
     * @throws UnsupportedOperationException if the method is not overloaded or otherwise
     */
    default T readArrayMappingObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features) {
        throw new UnsupportedOperationException();
    }

    /**
     * Reads an object from the specified JSON string using the provided features.
     *
     * @param str the JSON string to parse
     * @param features the JSON reader features to use
     * @return the deserialized object
     */
    default T readObject(String str, JSONReader.Feature... features) {
        try (JSONReader jsonReader = JSONReader.of(str, JSONFactory.createReadContext(features))) {
            return readObject(jsonReader, null, null, getFeatures());
        }
    }

    /**
     * Reads an object from the specified JSONReader.
     *
     * @param jsonReader the JSONReader to use for parsing
     * @return the deserialized object
     */
    default T readObject(JSONReader jsonReader) {
        return readObject(jsonReader, null, null, getFeatures());
    }

    /**
     * Reads an object from the specified JSONReader using the provided features.
     *
     * @param jsonReader the JSONReader to use for parsing
     * @param features the features to use
     * @return the deserialized object
     */
    default T readObject(JSONReader jsonReader, long features) {
        return readObject(jsonReader, null, null, features);
    }

    /**
     * Reads an object from the specified JSONReader with the given field type, field name, and features.
     *
     * @param jsonReader the JSONReader to use for parsing
     * @param fieldType the type of the field being read
     * @param fieldName the name of the field being read
     * @param features the features to use
     * @return the deserialized object
     * @throws JSONException if a suitable ObjectReader is not found
     */
    T readObject(JSONReader jsonReader, Type fieldType, Object fieldName, long features);
}
