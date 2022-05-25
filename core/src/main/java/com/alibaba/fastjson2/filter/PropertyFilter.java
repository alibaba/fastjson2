package com.alibaba.fastjson2.filter;

public interface PropertyFilter
        extends Filter {
    boolean apply(Object object, String name, Object value);
}
