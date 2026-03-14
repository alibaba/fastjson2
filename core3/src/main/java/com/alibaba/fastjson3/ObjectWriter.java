package com.alibaba.fastjson3;

import java.lang.reflect.Type;

/**
 * Core interface for serializing Java objects to JSON.
 * Each ObjectWriter handles one specific type. Implementations are cached
 * and reused by {@link ObjectMapper}.
 *
 * <pre>
 * // Custom writer example
 * public class PointWriter implements ObjectWriter&lt;Point&gt; {
 *     &#64;Override
 *     public void write(JSONGenerator generator, Object object, Object fieldName, Type fieldType, long features) {
 *         Point p = (Point) object;
 *         generator.startObject();
 *         generator.writeNameValue("x", p.x);
 *         generator.writeNameValue("y", p.y);
 *         generator.endObject();
 *     }
 * }
 * </pre>
 *
 * @param <T> the source type
 */
public interface ObjectWriter<T> {
    /**
     * Write an object as JSON.
     *
     * @param generator the JSON generator
     * @param object    the object to serialize
     * @param fieldName the field name (null for root value)
     * @param fieldType the declared field type (may include generic info)
     * @param features  feature flags for this write operation
     */
    void write(JSONGenerator generator, Object object, Object fieldName, Type fieldType, long features);

    /**
     * Write an object using default parameters.
     */
    default void write(JSONGenerator generator, Object object) {
        write(generator, object, null, null, 0);
    }
}
