package com.alibaba.fastjson2.filter;

public interface LabelFilter
        extends Filter {
    boolean apply(String label);
}
