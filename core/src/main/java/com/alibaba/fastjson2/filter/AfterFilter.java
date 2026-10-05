package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

public abstract class AfterFilter
        implements Filter {
    private static final ThreadLocal<JSONWriter> writerLocal = new ThreadLocal<>();

    /**
     * Invokes the callback with this writer and restores the enclosing callback's writer even if it fails.
     * <details><summary>中文</summary>
     * 使用指定的写入器调用回调，即使回调失败也会恢复外层回调的写入器。
     * </details>
     *
     * @param serializer the writer used by {@link #writeKeyValue(String, Object)}
     * @param object the object being serialized
     */
    public void writeAfter(JSONWriter serializer, Object object) {
        JSONWriter last = writerLocal.get();
        writerLocal.set(serializer);
        try {
            writeAfter(object);
        } finally {
            writerLocal.set(last);
        }
    }

    protected final void writeKeyValue(String key, Object value) {
        JSONWriter serializer = writerLocal.get();
        boolean ref = serializer.containsReference(value);
        serializer.writeName(key);
        serializer.writeColon();
        serializer.writeAny(value);
        if (!ref) {
            serializer.removeReference(value);
        }
    }

    public abstract void writeAfter(Object object);
}
