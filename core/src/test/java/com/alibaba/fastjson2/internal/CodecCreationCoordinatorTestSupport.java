package com.alibaba.fastjson2.internal;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

public final class CodecCreationCoordinatorTestSupport {
    private CodecCreationCoordinatorTestSupport() {
    }

    public static CodecCreationCoordinator.Scope acquire(
            ConcurrentMap<Type, CodecCreationCoordinator.LockEntry> locks,
            Type type,
            long waitNanos,
            long fallbackIntervalNanos
    ) {
        return CodecCreationCoordinator.acquire(locks, type, waitNanos, fallbackIntervalNanos);
    }
}
