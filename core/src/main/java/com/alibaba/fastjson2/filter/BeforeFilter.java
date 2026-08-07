package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

public abstract class BeforeFilter
        implements Filter {
    private static final ThreadLocal<JSONWriter> serializerLocal = new ThreadLocal<>();

    public void writeBefore(JSONWriter serializer, Object object) {
        JSONWriter last = serializerLocal.get();
        serializerLocal.set(serializer);
        writeBefore(object);
        serializerLocal.set(last);
    }

    protected final void writeKeyValue(String key, Object value) {
        JSONWriter serializer = serializerLocal.get();
        boolean ref = serializer.containsReference(value);
        serializer.writeName(key);
        serializer.writeColon();
        serializer.writeAny(value);
        if (!ref) {
            serializer.removeReference(value);
        }
    }

    public abstract void writeBefore(Object object);
}
