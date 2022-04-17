package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

final class ObjectWriterImplInt64ValueArray extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt64ValueArray INSTANCE = new ObjectWriterImplInt64ValueArray();

    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[J");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[J");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeInt64((long[]) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeInt64((long[]) object);
    }
}
