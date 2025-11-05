package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple implementation of PropertyPreFilter that filters properties by name inclusion/exclusion.
 * Supports class-specific filtering and maximum nesting depth control.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Include only specific properties
 * SimplePropertyPreFilter filter = new SimplePropertyPreFilter("id", "name", "email");
 * String json = JSON.toJSONString(user, filter);
 *
 * // Include properties for specific class
 * SimplePropertyPreFilter userFilter = new SimplePropertyPreFilter(User.class, "id", "name");
 * String json2 = JSON.toJSONString(user, userFilter);
 *
 * // Limit nesting depth
 * SimplePropertyPreFilter depthFilter = new SimplePropertyPreFilter();
 * depthFilter.setMaxLevel(2);
 * }</pre>
 */
public class SimplePropertyPreFilter
        implements PropertyPreFilter {
    private final Class<?> clazz;
    private final Set<String> includes = new HashSet<>();
    private final Set<String> excludes = new HashSet<>();
    private int maxLevel;

    /**
     * Constructs a filter that includes only the specified properties.
     *
     * @param properties the property names to include
     */
    public SimplePropertyPreFilter(String... properties) {
        this(null, properties);
    }

    /**
     * Constructs a filter that includes only the specified properties for a specific class.
     *
     * @param clazz the class to filter, or null to filter all classes
     * @param properties the property names to include
     */
    public SimplePropertyPreFilter(Class<?> clazz, String... properties) {
        super();
        this.clazz = clazz;
        for (String item : properties) {
            if (item != null) {
                this.includes.add(item);
            }
        }
    }

    /**
     * Returns the maximum nesting level for serialization.
     *
     * @return the maximum level, or 0 if no limit
     * @since 1.2.9
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Sets the maximum nesting level for serialization.
     * Properties beyond this depth will not be serialized.
     *
     * @param maxLevel the maximum nesting level, or 0 for no limit
     * @since 1.2.9
     */
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * Returns the class this filter applies to.
     *
     * @return the target class, or null if filtering all classes
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * Returns the set of property names to include.
     *
     * @return the includes set
     */
    public Set<String> getIncludes() {
        return includes;
    }

    /**
     * Returns the set of property names to exclude.
     *
     * @return the excludes set
     */
    public Set<String> getExcludes() {
        return excludes;
    }

    @Override
    public boolean process(JSONWriter writer, Object source, String name) {
        if (source == null) {
            return true;
        }

        if (clazz != null && !clazz.isInstance(source)) {
            return true;
        }

        if (this.excludes.contains(name)) {
            return false;
        }

        if (maxLevel > 0 && writer.level() > maxLevel) {
            return false;
        }

        return includes.size() == 0
                || includes.contains(name);
    }
}
