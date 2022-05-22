package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

final class ObjectWriterImplInt32ValueArray
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt32ValueArray INSTANCE = new ObjectWriterImplInt32ValueArray();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[I");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[I");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeInt32((int[]) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeInt32((int[]) object);
    }
}
