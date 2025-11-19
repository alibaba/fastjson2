package com.alibaba.fastjson2.filter;

import java.util.ArrayList;
import java.util.List;

public interface LabelFilter
        extends Filter {
    boolean apply(String label);

    static LabelFilter compose(LabelFilter before, LabelFilter after) {
        if (before instanceof CompositeLabelFilter) {
            ((CompositeLabelFilter) before).add(after);
            return before;
        }

        return new CompositeLabelFilter(before, after);
    }

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
}
