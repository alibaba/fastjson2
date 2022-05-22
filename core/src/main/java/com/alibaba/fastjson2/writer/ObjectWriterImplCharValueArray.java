package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

final class ObjectWriterImplCharValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplCharValueArray INSTANCE = new ObjectWriterImplCharValueArray();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[C");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[C");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }
        jsonWriter.writeString((char[]) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeString((char[]) object);
    }
}
