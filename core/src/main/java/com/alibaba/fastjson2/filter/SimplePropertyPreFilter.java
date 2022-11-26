package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

import java.util.HashSet;
import java.util.Set;

public class SimplePropertyPreFilter
        implements PropertyPreFilter {
    private final Class<?> clazz;
    private final Set<String> includes = new HashSet<String>();
    private final Set<String> excludes = new HashSet<String>();
    private int maxLevel;

    public SimplePropertyPreFilter(String... properties) {
        this(null, properties);
    }

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
     * @since 1.2.9
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * @since 1.2.9
     */
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Set<String> getIncludes() {
        return includes;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    @Override
    public boolean process(JSONWriter writer, Object source, String name) {
        if (source == null) {
            return true;
        }

        if (clazz != null && !clazz.isInstance(source)) {
            return excludes.size() != 0 && includes.isEmpty() && maxLevel == 0;
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
