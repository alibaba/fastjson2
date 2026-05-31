package com.alibaba.fastjson2.filter;

import java.util.ArrayList;
import java.util.List;

final class CompositePropertyFilter
        implements PropertyFilter {
    private final List<PropertyFilter> filters = new ArrayList<>();

    public CompositePropertyFilter(PropertyFilter... filters) {
        for (PropertyFilter filter : filters) {
            if (filter != null) {
                this.filters.add(filter);
            }
        }
    }

    public void add(PropertyFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
    }

    @Override
    public boolean apply(Object object, String name, Object value) {
        for (PropertyFilter filter : filters) {
            if (!filter.apply(object, name, value)) {
                return false;
            }
        }
        return true;
    }
}
