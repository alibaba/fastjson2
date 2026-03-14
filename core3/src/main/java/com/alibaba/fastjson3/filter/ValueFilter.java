package com.alibaba.fastjson3.filter;

/**
 * Filter that transforms a property value during serialization.
 * The returned value replaces the original value in JSON output.
 *
 * <pre>
 * ValueFilter filter = (source, name, value) -&gt; {
 *     if ("salary".equals(name)) return "***";
 *     return value;
 * };
 * </pre>
 */
@FunctionalInterface
public interface ValueFilter {
    /**
     * @param source the object being serialized
     * @param name   the property name
     * @param value  the original property value
     * @return the value to write (may be the original or a replacement)
     */
    Object apply(Object source, String name, Object value);
}
