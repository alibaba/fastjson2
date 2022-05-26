package com.alibaba.fastjson.serializer;

public class ContextAutoTypeBeforeHandler
        extends com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler
        implements SerializeFilter {
    public ContextAutoTypeBeforeHandler(String[] acceptNames) {
        super(acceptNames);
    }
}
