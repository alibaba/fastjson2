package com.alibaba.fastjson.serializer;

public class Labels {
    private static class DefaultLabelFilter
            extends com.alibaba.fastjson2.filter.Labels.DefaultLabelFilter
            implements LabelFilter {
        public DefaultLabelFilter(String[] includes, String[] excludes) {
            super(includes, excludes);
        }
    }

    public static LabelFilter includes(String... views) {
        return new DefaultLabelFilter(views, null);
    }

    public static LabelFilter excludes(String... views) {
        return new DefaultLabelFilter(null, views);
    }
}
