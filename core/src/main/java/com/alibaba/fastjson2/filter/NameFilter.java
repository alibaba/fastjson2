package com.alibaba.fastjson2.filter;

public interface NameFilter extends Filter {
    String process(Object object, String name, Object value);
}
