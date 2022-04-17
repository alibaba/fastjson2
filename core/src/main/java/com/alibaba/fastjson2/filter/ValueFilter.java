package com.alibaba.fastjson2.filter;

public interface ValueFilter extends Filter {
    Object apply(Object object, String name, Object value);
}
