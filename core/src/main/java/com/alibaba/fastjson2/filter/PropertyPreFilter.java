package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

public interface PropertyPreFilter
        extends Filter {
    boolean process(JSONWriter writer, Object source, String name);

    static PropertyPreFilter compose(PropertyPreFilter before, PropertyPreFilter after) {
        if (before instanceof CompositePropertyPreFilter) {
            ((CompositePropertyPreFilter) before).add(after);
            return before;
        }

        return new CompositePropertyPreFilter(before, after);
    }
}
