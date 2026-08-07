package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

public abstract class AfterFilter
        implements Filter {
    private static final ThreadLocal<JSONWriter> writerLocal = new ThreadLocal<>();

    public void writeAfter(JSONWriter serializer, Object object) {
        JSONWriter last = writerLocal.get();
        writerLocal.set(serializer);
        writeAfter(object);
        writerLocal.set(last);
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
