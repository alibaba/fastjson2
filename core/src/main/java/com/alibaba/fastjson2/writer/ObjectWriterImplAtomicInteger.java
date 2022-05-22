package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

final class ObjectWriterImplAtomicInteger
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplAtomicInteger INSTANCE = new ObjectWriterImplAtomicInteger(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("AtomicInteger");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("AtomicInteger");

    final Class defineClass;

    public ObjectWriterImplAtomicInteger(Class defineClass) {
        this.defineClass = defineClass;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        AtomicInteger atomic = (AtomicInteger) object;
        if (jsonWriter.isWriteTypeInfo(atomic, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeInt32(atomic.intValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        AtomicInteger atomic = (AtomicInteger) object;
        jsonWriter.writeInt32(atomic.intValue());
    }
}
