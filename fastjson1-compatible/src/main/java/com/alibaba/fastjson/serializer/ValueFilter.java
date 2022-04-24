package com.alibaba.fastjson.serializer;

public interface ValueFilter extends SerializeFilter, com.alibaba.fastjson2.filter.ValueFilter {

    Object process(Object object, String name, Object value);

    @Override
    default Object apply(Object object, String name, Object value) {
        return process(object, name, value);
    }
}
