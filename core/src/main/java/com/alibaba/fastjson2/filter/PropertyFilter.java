package com.alibaba.fastjson2.filter;

public interface PropertyFilter extends Filter {
    boolean process(Object object, String name, Object value);
}
