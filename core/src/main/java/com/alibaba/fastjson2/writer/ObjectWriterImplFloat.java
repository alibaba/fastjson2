package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

final class ObjectWriterImplFloat
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplFloat INSTANCE = new ObjectWriterImplFloat();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeFloat(((Float) object).floatValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeFloat(((Float) object).floatValue());
        if (((jsonWriter.getFeatures() | features) & JSONWriter.Feature.WriteClassName.mask) != 0
                && fieldType != Float.class && fieldType != float.class) {
            jsonWriter.writeRaw('F');
        }
    }
}
