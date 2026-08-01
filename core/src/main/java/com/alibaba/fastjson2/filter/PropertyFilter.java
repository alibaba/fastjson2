package com.alibaba.fastjson2.filter;

public interface PropertyFilter
        extends Filter {
    boolean apply(Object object, String name, Object value);

    static PropertyFilter compose(PropertyFilter before, PropertyFilter after) {
        if (before instanceof CompositePropertyFilter) {
            ((CompositePropertyFilter) before).add(after);
            return before;
        }

        return new CompositePropertyFilter(before, after);
    }
}
