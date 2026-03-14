package com.alibaba.fastjson3;

import java.lang.reflect.Type;

/**
 * Core interface for deserializing JSON to Java objects.
 * Each ObjectReader handles one specific type. Implementations are cached
 * and reused by {@link ObjectMapper}.
 *
 * <pre>
 * // Custom reader example
 * public class PointReader implements ObjectReader&lt;Point&gt; {
 *     &#64;Override
 *     public Point readObject(JSONParser parser, Type fieldType, Object fieldName, long features) {
 *         parser.readObject(); // read as JSONObject first
 *         ...
 *     }
 * }
 * </pre>
 *
 * @param <T> the target type
 */
public interface ObjectReader<T> {
    /**
     * Read a JSON value and convert it to an object of the target type.
     *
     * @param parser    the JSON parser
     * @param fieldType the declared field type (may include generic info)
     * @param fieldName the field name (null for root value)
     * @param features  feature flags for this read operation
     * @return the deserialized object
     */
    T readObject(JSONParser parser, Type fieldType, Object fieldName, long features);

    /**
     * Read a JSON value using default parameters.
     */
    default T readObject(JSONParser parser) {
        return readObject(parser, null, null, 0);
    }

    /**
     * Fast path for UTF-8 byte[] parsing. Bypasses instanceof check.
     * Default delegates to readObject.
     */
    default T readObjectUTF8(JSONParser.UTF8 utf8, long features) {
        return readObject(utf8, null, null, features);
    }

    /**
     * Create a new instance of the target type.
     * Override this for custom construction (e.g., builder pattern, factory method).
     *
     * @param features feature flags
     * @return a new instance, or null if this reader doesn't support direct creation
     */
    default T createInstance(long features) {
        return null;
    }

    /**
     * Return the target class this reader handles.
     */
    default Class<T> getObjectClass() {
        return null;
    }

    /**
     * Read a single field value from UTF-8 input and set it on the instance.
     * Used by ASM-generated readers to delegate complex field types to the reflection reader.
     *
     * @param utf8       the UTF-8 parser (positioned at the value start)
     * @param instance   the target bean
     * @param fieldIndex the field index in the fieldReaders array
     * @param features   feature flags
     */
    default void readFieldUTF8(JSONParser.UTF8 utf8, Object instance, int fieldIndex, long features) {
        throw new UnsupportedOperationException();
    }
}
