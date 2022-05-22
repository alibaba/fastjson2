package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

final class ObjectWriterImplDoubleValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplDoubleValueArray INSTANCE = new ObjectWriterImplDoubleValueArray();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[D");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[D");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeDouble((double[]) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeDouble((double[]) object);
    }
}
