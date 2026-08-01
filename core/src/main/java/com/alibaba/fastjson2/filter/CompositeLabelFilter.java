package com.alibaba.fastjson2.filter;

import java.util.ArrayList;
import java.util.List;

final class CompositeLabelFilter
        implements LabelFilter {
    private final List<LabelFilter> filters = new ArrayList<>();

    CompositeLabelFilter(LabelFilter... filters) {
        for (LabelFilter filter : filters) {
            if (filter != null) {
                this.filters.add(filter);
            }
        }
    }

    void add(LabelFilter filter) {
        if (filter != null) {
            this.filters.add(filter);
        }
    }

    @Override
    public boolean apply(String label) {
        for (LabelFilter filter : filters) {
            if (!filter.apply(label)) {
                return false;
            }
        }
        return true;
    }
}
