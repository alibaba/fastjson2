package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplInt8
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplInt8 INSTANCE = new ObjectWriterImplInt8();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        jsonWriter.writeInt8(((Number) object).byteValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(((Number) object).intValue());
        if (((jsonWriter.getFeatures() | features) & JSONWriter.Feature.WriteClassName.mask) != 0
                && fieldType != Byte.class && fieldType != byte.class) {
            jsonWriter.writeRaw('B');
        }
    }
}
