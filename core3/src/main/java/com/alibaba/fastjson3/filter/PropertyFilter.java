package com.alibaba.fastjson3.filter;

/**
 * Filter that controls whether a property should be serialized.
 * Return {@code false} to exclude the property from output.
 *
 * <pre>
 * PropertyFilter filter = (source, name, value) -&gt; !"password".equals(name);
 * </pre>
 */
@FunctionalInterface
public interface PropertyFilter {
    /**
     * @param source the object being serialized
     * @param name   the property name
     * @param value  the property value
     * @return true to include this property, false to exclude
     */
    boolean apply(Object source, String name, Object value);
}
