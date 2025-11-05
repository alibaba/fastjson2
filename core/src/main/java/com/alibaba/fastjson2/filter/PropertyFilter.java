package com.alibaba.fastjson2.filter;

/**
 * Filter interface for selectively including or excluding properties during JSON serialization.
 * Determines whether a property should be serialized based on custom logic.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Exclude null values
 * PropertyFilter nonNullFilter = (object, name, value) -> value != null;
 *
 * // Include only specific properties
 * PropertyFilter includeFilter = (object, name, value) ->
 *     "id".equals(name) || "name".equals(name);
 *
 * // Exclude sensitive properties
 * PropertyFilter excludeSensitiveFilter = (object, name, value) ->
 *     !name.contains("password") && !name.contains("secret");
 *
 * // Use with JSON serialization
 * String json = JSON.toJSONString(user, nonNullFilter);
 * }</pre>
 */
public interface PropertyFilter
        extends Filter {
    /**
     * Determines whether a property should be included in JSON serialization.
     *
     * @param object the object being serialized
     * @param name the property name
     * @param value the property value
     * @return true to include the property, false to exclude it
     */
    boolean apply(Object object, String name, Object value);
}
