package com.alibaba.fastjson2.filter;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Filter interface for transforming property values during JSON serialization.
 * Allows modification of values before they are written to JSON output.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Mask sensitive values
 * ValueFilter maskFilter = (object, name, value) -> {
 *     if ("password".equals(name) && value != null) {
 *         return "******";
 *     }
 *     return value;
 * };
 *
 * // Convert null values to empty strings
 * ValueFilter nullToEmptyFilter = (object, name, value) ->
 *     value == null ? "" : value;
 *
 * // Use with JSON serialization
 * String json = JSON.toJSONString(user, maskFilter);
 * }</pre>
 */
public interface ValueFilter
        extends Filter {
    /**
     * Transforms the property value during serialization.
     *
     * @param object the object being serialized
     * @param name the property name
     * @param value the original property value
     * @return the transformed value to write to JSON
     */
    Object apply(Object object, String name, Object value);

    static ValueFilter compose(ValueFilter before, ValueFilter after) {
        return (object, name, value) ->
                after.apply(
                        object,
                        name,
                        before.apply(object, name, value)
                );
    }

    static ValueFilter of(final String name, Function function) {
        return (object, fieldName, fieldValue)
                -> name == null || name.equals(fieldName)
                ? function.apply(fieldValue)
                : fieldValue;
    }

    static ValueFilter of(final String name, Map map) {
        return (object, fieldName, fieldValue) -> {
            if (name == null || name.equals(fieldName)) {
                Object o = map.get(fieldValue);
                if (o != null || map.containsKey(fieldValue)) {
                    return o;
                }
            }
            return fieldValue;
        };
    }

    static ValueFilter of(Predicate<String> nameMatcher, Function function) {
        return (object, fieldName, fieldValue)
                -> nameMatcher == null || nameMatcher.test(fieldName)
                ? function.apply(fieldValue)
                : fieldValue;
    }
}
