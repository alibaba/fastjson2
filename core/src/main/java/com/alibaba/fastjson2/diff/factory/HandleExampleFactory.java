package com.alibaba.fastjson2.diff.factory;

import com.alibaba.fastjson2.diff.handle.*;
import com.alibaba.fastjson2.diff.object.AbstractObjectHandle;
import com.alibaba.fastjson2.diff.object.SimpleObjectHandle;

import java.util.HashMap;
import java.util.Map;

public class HandleExampleFactory {
    /**
     * Handle the instance bucket of an object
     */
    private static final Map<Class<?>, Handle> bucket = new HashMap<>();

    static {
        bucket.put(SimpleArrayHandle.class, new SimpleArrayHandle());
        bucket.put(SimpleObjectHandle.class, new SimpleObjectHandle());
        bucket.put(IntricacyArrayHandle.class, new IntricacyArrayHandle());
        bucket.put(ObjectArrayHandle.class, new ObjectArrayHandle());
        bucket.put(MultidimensionalArrayHandle.class, new MultidimensionalArrayHandle());
    }

    public static Handle getHandle(Class<?> handle) {
        if (!AbstractArrayHandle.class.isAssignableFrom(handle) && !AbstractObjectHandle.class.isAssignableFrom(handle)) {
            return bucket.get(AbstractObjectHandle.class);
        }
        return bucket.get(handle);
    }
}
