package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

final class ObjectWriterImplAtomicReference
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplAtomicReference INSTANCE = new ObjectWriterImplAtomicReference(null);

    final Class defineClass;

    public ObjectWriterImplAtomicReference(Class defineClass) {
        this.defineClass = defineClass;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        AtomicReference atomic = (AtomicReference) object;
        Object ref = atomic.get();
        if (ref == null) {
            jsonWriter.writeNull();
        }

        jsonWriter.writeAny(ref);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        AtomicReference atomic = (AtomicReference) object;
        Object ref = atomic.get();
        if (ref == null) {
            jsonWriter.writeNull();
        }

        jsonWriter.writeAny(ref);
    }
}
