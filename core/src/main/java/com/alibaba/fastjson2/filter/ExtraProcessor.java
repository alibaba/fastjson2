package com.alibaba.fastjson2.filter;

import java.lang.reflect.Type;

/**
 * Interface for handling extra properties during JSON deserialization.
 * Processes properties that don't map to any field in the target class.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * public class User {
 *     private String name;
 *     private Map<String, Object> extra = new HashMap<>();
 *
 *     // getters and setters
 * }
 *
 * ExtraProcessor processor = new ExtraProcessor() {
 *     @Override
 *     public void processExtra(Object object, String key, Object value) {
 *         if (object instanceof User) {
 *             ((User) object).getExtra().put(key, value);
 *         }
 *     }
 * };
 *
 * String json = "{\"name\":\"Alice\",\"age\":25,\"city\":\"NYC\"}";
 * User user = JSON.parseObject(json, User.class, processor);
 * // user.name = "Alice", user.extra = {"age":25, "city":"NYC"}
 * }</pre>
 */
public interface ExtraProcessor
        extends Filter {
    /**
     * Returns the expected type for the extra property with the given name.
     * Used to properly deserialize the value.
     *
     * @param fieldName the name of the extra property
     * @return the expected type, defaults to Object.class
     */
    default Type getType(String fieldName) {
        return Object.class;
    }

    /**
     * Processes an extra property that doesn't map to any field.
     *
     * @param object the object being deserialized
     * @param key the property name
     * @param value the property value
     */
    void processExtra(Object object, String key, Object value);
}
