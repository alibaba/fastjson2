package com.alibaba.fastjson3.filter;

/**
 * Filter that transforms a property name during serialization.
 * The returned name replaces the original property name in JSON output.
 *
 * <pre>
 * NameFilter filter = (source, name, value) -&gt; name.toUpperCase();
 * </pre>
 */
@FunctionalInterface
public interface NameFilter {
    /**
     * @param source the object being serialized
     * @param name   the original property name
     * @param value  the property value
     * @return the name to use in JSON output
     */
    String apply(Object source, String name, Object value);
}
