package com.alibaba.fastjson2.internal;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class PropertyAccessorFactoryV extends PropertyAccessorFactory {
    private final MethodHandles.Lookup lookup;

    public PropertyAccessorFactoryV(MethodHandles.Lookup lookup) {
        this.lookup = lookup;
    }

    protected PropertyAccessor createInternal(Field field) {
        return new FieldAccessorV(lookup, field);
    }
}
