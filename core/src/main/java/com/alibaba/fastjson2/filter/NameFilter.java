package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.util.BeanUtils;

import java.util.function.Function;

/**
 * Filter interface for customizing property names during JSON serialization.
 * Allows transformation of property names before they are written to JSON output.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Convert all property names to uppercase
 * NameFilter upperCaseFilter = (object, name, value) -> name.toUpperCase();
 *
 * // Add prefix to property names
 * NameFilter prefixFilter = (object, name, value) -> "prop_" + name;
 *
 * // Use with JSON serialization
 * String json = JSON.toJSONString(obj, upperCaseFilter);
 * }</pre>
 */
public interface NameFilter
        extends Filter {
    /**
     * Transforms the property name during serialization.
     *
     * @param object the object being serialized
     * @param name the original property name
     * @param value the property value
     * @return the transformed property name to use in JSON output
     */
    String process(Object object, String name, Object value);

    static NameFilter of(PropertyNamingStrategy namingStrategy) {
        return (object, name, value) -> BeanUtils.fieldName(name, namingStrategy.name());
    }

    static NameFilter compose(NameFilter before, NameFilter after) {
        return (object, name, value) ->
                after.process(
                        object,
                        before.process(object, name, value),
                        value
                );
    }

    static NameFilter of(Function<String, String> function) {
        return (object, name, value) -> function.apply(name);
    }
}
