package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

/**
 * Filter interface for pre-filtering properties before serialization.
 * Similar to PropertyFilter but provides access to the JSONWriter for more advanced filtering.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * PropertyPreFilter customFilter = new PropertyPreFilter() {
 *     @Override
 *     public boolean process(JSONWriter writer, Object source, String name) {
 *         // Only serialize properties when writer depth is less than 3
 *         if (writer.level() > 3) {
 *             return false;
 *         }
 *         return true;
 *     }
 * };
 *
 * String json = JSON.toJSONString(obj, customFilter);
 * }</pre>
 */
public interface PropertyPreFilter
        extends Filter {
    /**
     * Determines whether a property should be serialized.
     *
     * @param writer the JSONWriter being used for serialization
     * @param source the object being serialized
     * @param name the property name
     * @return true to include the property, false to exclude it
     */
    boolean process(JSONWriter writer, Object source, String name);
}
