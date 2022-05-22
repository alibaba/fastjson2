package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

final class ObjectWriterImplAtomicBoolean
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplAtomicBoolean INSTANCE = new ObjectWriterImplAtomicBoolean();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeBooleanNull();
            return;
        }
        jsonWriter.writeBool(
                ((AtomicBoolean) object).get());
    }
}
