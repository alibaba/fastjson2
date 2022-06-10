package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.PropertyNamingStrategy;
import com.alibaba.fastjson2.util.BeanUtils;

public class PascalNameFilter
        implements NameFilter {
    @Override
    public String process(Object source, String name, Object value) {
        return BeanUtils.fieldName(name, PropertyNamingStrategy.PascalCase.name());
    }
}
