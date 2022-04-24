package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson2.filter.ValueFilter;

public interface ContextValueFilter extends SerializeFilter, ValueFilter {
    Object process(BeanContext context, Object object, String name, Object value);

    @Override
    default Object apply(Object object, String name, Object value) {
        return process(null, object, name, value);
    }
}
