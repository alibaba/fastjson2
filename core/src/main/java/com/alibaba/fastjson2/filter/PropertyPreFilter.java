package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

import java.util.ArrayList;
import java.util.List;

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
}
