package com.alibaba.fastjson.serializer;

public class SimplePropertyPreFilter extends com.alibaba.fastjson2.filter.SimplePropertyPreFilter implements SerializeFilter {
    public SimplePropertyPreFilter(String... properties) {
        super(properties);
    }

    public SimplePropertyPreFilter(Class<?> clazz, String... properties) {
        super(clazz, properties);
    }
}
