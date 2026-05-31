package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

import java.util.ArrayList;
import java.util.List;

final class CompositePropertyPreFilter
        implements PropertyPreFilter {
    private final List<PropertyPreFilter> filters = new ArrayList<>();

    public CompositePropertyPreFilter(PropertyPreFilter... filters) {
        for (PropertyPreFilter filter : filters) {
            if (filter != null) {
                this.filters.add(filter);
            }
        }
    }

    public void add(PropertyPreFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
    }

    @Override
    public boolean process(JSONWriter writer, Object source, String name) {
        for (PropertyPreFilter filter : filters) {
            if (!filter.process(writer, source, name)) {
                return false;
            }
        }
        return true;
    }
}
