package com.alibaba.fastjson2.adapter.jackson.core.json;

import com.alibaba.fastjson2.adapter.jackson.core.FormatFeature;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;

public enum JsonReadFeature
        implements FormatFeature {
    ALLOW_UNESCAPED_CONTROL_CHARS(false, JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);

    final boolean defaultState;
    final int mask;
    final JsonParser.Feature mappedFeature;

    JsonReadFeature(
            boolean defaultState,
            JsonParser.Feature mapTo
    ) {
        this.defaultState = defaultState;
        mask = (1 << ordinal());
        mappedFeature = mapTo;
    }

    public JsonParser.Feature mappedFeature() {
        return mappedFeature;
    }
}
