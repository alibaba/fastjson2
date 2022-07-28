package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

class ObjectWriterImplBoolValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplBoolValueArray INSTANCE = new ObjectWriterImplBoolValueArray();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[Z");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[Z");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }
        jsonWriter.writeBool((boolean[]) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeBool((boolean[]) object);
    }
}
