package com.alibaba.fastjson.serializer;

public class BeanContext
        extends com.alibaba.fastjson2.filter.BeanContext {
    public BeanContext(com.alibaba.fastjson2.filter.BeanContext ctx) {
        super(
                ctx.getBeanClass(),
                ctx.getMethod(),
                ctx.getField(),
                ctx.getName(),
                ctx.getLabel(),
                ctx.getFieldClass(),
                ctx.getFieldType(),
                ctx.getFeatures(),
                ctx.getFormat()
        );
    }
}
