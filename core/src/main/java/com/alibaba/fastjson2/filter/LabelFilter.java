package com.alibaba.fastjson2.filter;

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
}
