package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

final class ObjectWriterImplAtomicLong
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplAtomicLong INSTANCE = new ObjectWriterImplAtomicLong(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("AtomicLong");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("AtomicLong");

    final Class defineClass;

    public ObjectWriterImplAtomicLong(Class defineClass) {
        this.defineClass = defineClass;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        AtomicLong atomic = (AtomicLong) object;
        if (jsonWriter.isWriteTypeInfo(atomic, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        long longValue = atomic.longValue();
        jsonWriter.writeInt64(longValue);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt64(
                ((Number) object).longValue());
    }
}
