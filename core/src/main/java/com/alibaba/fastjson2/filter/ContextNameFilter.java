package com.alibaba.fastjson2.filter;

public interface ContextNameFilter
        extends Filter {
    String process(BeanContext context, Object object, String name, Object value);
}
