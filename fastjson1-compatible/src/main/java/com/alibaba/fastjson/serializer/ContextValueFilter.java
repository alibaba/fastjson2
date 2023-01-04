package com.alibaba.fastjson.serializer;

public interface ContextValueFilter
        extends com.alibaba.fastjson2.filter.ContextValueFilter, SerializeFilter {
    @Override
    default Object process(com.alibaba.fastjson2.filter.BeanContext context, Object object, String name, Object value) {
        return process(new com.alibaba.fastjson.serializer.BeanContext(context), object, name, value);
    }

    Object process(BeanContext context, Object object, String name, Object value);
}
