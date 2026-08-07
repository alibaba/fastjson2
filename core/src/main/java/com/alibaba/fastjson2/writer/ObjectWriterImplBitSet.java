package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.BitSet;

final class ObjectWriterImplBitSet
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplBitSet INSTANCE = new ObjectWriterImplBitSet();

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        BitSet bitSet = (BitSet) object;

        byte[] bytes = bitSet.toByteArray();
        jsonWriter.writeBinary(bytes);
    }
}
