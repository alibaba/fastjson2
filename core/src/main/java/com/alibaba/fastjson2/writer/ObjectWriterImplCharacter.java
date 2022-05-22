package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.Type;

final class ObjectWriterImplCharacter
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplCharacter INSTANCE = new ObjectWriterImplCharacter();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("C");
    static final long JSONB_TYPE_HASH = Fnv.hashCode64("C");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        if (jsonWriter.isWriteTypeInfo(object)) {
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeString(new char[]{(Character) object});
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeString(new char[]{(Character) object});
    }
}
