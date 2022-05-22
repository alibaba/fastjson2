package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.Map;

final class ObjectWriterImplMapEntry
        extends ObjectWriterBaseModule.PrimitiveImpl {
    static final ObjectWriterImplMapEntry INSTANCE = new ObjectWriterImplMapEntry();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Map.Entry entry = (Map.Entry) object;
        if (entry == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.startArray(2);
        jsonWriter.writeAny(entry.getKey());
        jsonWriter.writeAny(entry.getValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        Map.Entry entry = (Map.Entry) object;
        if (entry == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.startObject();
        jsonWriter.writeAny(entry.getKey());
        jsonWriter.writeColon();
        jsonWriter.writeAny(entry.getValue());
        jsonWriter.endObject();
    }
}
