package com.alibaba.fastjson.serializer;

import java.util.Set;

public class SimplePropertyPreFilter
        extends com.alibaba.fastjson2.filter.SimplePropertyPreFilter
        implements SerializeFilter {
    public SimplePropertyPreFilter(String... properties) {
        super(properties);
    }

    public SimplePropertyPreFilter(Class<?> clazz, String... properties) {
        super(clazz, properties);
    }

    @Override
    public Set<String> getIncludes() {
        return super.getIncludes();
    }

    @Override
    public Set<String> getExcludes() {
        return super.getExcludes();
    }
}
