package com.alibaba.fastjson2.adapter.jackson.dataformat.csv;

import com.alibaba.fastjson2.adapter.jackson.core.FormatFeature;

public class CsvParser {
    public enum Feature
            implements FormatFeature {
        TRIM_SPACES(false),
        WRAP_AS_ARRAY(false),
        IGNORE_TRAILING_UNMAPPABLE(false),
        SKIP_EMPTY_LINES(false),
        ALLOW_TRAILING_COMMA(true),
        ALLOW_COMMENTS(false),
        FAIL_ON_MISSING_COLUMNS(false),
        INSERT_NULLS_FOR_MISSING_COLUMNS(false),
        EMPTY_STRING_AS_NULL(false);

        final boolean defaultState;
        final int mask;

        Feature(boolean defaultState) {
            this.defaultState = defaultState;
            this.mask = 1 << this.ordinal();
        }
    }
}
