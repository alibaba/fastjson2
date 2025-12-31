package com.alibaba.fastjson2.internal;

import java.lang.reflect.Field;

public final class PropertyAccessorFactoryUnsafe
        extends PropertyAccessorFactory {
    protected PropertyAccessor createInternal(Field field) {
        return new FieldAccessorUnsafe(field);
    }
}
