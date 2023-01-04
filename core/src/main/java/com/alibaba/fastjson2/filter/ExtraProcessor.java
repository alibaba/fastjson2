package com.alibaba.fastjson2.filter;

import java.lang.reflect.Type;

public interface ExtraProcessor
        extends Filter {
    default Type getType(String fieldName) {
        return Object.class;
    }

    void processExtra(Object object, String key, Object value);
}
