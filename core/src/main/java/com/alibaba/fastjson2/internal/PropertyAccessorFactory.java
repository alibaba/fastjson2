package com.alibaba.fastjson2.internal;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PropertyAccessorFactory {
    protected final ConcurrentMap<Object, PropertyAccessor> cache = new ConcurrentHashMap<>();

    public PropertyAccessor create(Field field) {
        PropertyAccessor accessor = cache.get(field);
        if (accessor == null) {
            accessor = createInternal(field);
            cache.put(field, accessor);
        }
        return accessor;
    }

    protected PropertyAccessor createInternal(Field field) {
        return new FieldAccessorReflect(field);
    }
}
