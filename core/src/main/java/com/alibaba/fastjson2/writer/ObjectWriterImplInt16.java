package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplInt16 extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt16 INSTANCE = new ObjectWriterImplInt16();
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("S");

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        jsonWriter.writeInt16(((Short) object).shortValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(((Short) object).shortValue());
    }
}
