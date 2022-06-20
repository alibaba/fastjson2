package com.alibaba.fastjson2.filter;

public interface ContextValueFilter
        extends Filter {
    Object process(BeanContext context, Object object, String name, Object value);
}
