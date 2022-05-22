package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

final class ObjectWriterImplFloatValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplFloatValueArray INSTANCE = new ObjectWriterImplFloatValueArray();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[F");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[F");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeFloat((float[]) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeFloat((float[]) object);
    }
}
