package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

final class ObjectWriterImplInt8ValueArray extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt8ValueArray INSTANCE = new ObjectWriterImplInt8ValueArray();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("[B");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("[B");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (jsonWriter.isWriteTypeInfo(object, fieldType)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        byte[] bytes = (byte[]) object;
        jsonWriter.writeBinary(bytes);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        byte[] array = (byte[]) object;
        jsonWriter.startArray();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                jsonWriter.writeComma();
            }
            jsonWriter.writeInt32(array[i]);
        }
        jsonWriter.endArray();
    }
}
